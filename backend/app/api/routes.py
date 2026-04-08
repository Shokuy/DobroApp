from sqlalchemy import func, select
from sqlalchemy.orm import Session
from fastapi import APIRouter, Depends, HTTPException

from app.db import get_db
from app.models import HelpRequest, Reward, User, WalletTransaction
from app.schemas import (
    AuthOut,
    LeaderboardOut,
    ProfileOut,
    RequestAcceptIn,
    RequestCompleteIn,
    RequestCreateIn,
    RequestOut,
    RewardOut,
    RoleSelectIn,
    WalletOut,
)
from app.security import create_token

router = APIRouter()


@router.post("/auth/role", response_model=AuthOut)
def select_role(payload: RoleSelectIn, db: Session = Depends(get_db)) -> AuthOut:
    user = db.scalar(
        select(User).where(
            User.role == payload.role,
            User.full_name == payload.full_name.strip(),
        ).limit(1)
    )
    if user is None:
        user = User(
            role=payload.role,
            full_name=payload.full_name.strip(),
            district="Центральный",
        )
        db.add(user)
        db.commit()
        db.refresh(user)
    return AuthOut(
        user_id=user.id,
        role=user.role,
        full_name=user.full_name,
        token=create_token(user.id, user.role),
    )


@router.get("/requests/open", response_model=list[RequestOut])
def list_open_requests(district: str | None = None, db: Session = Depends(get_db)) -> list[RequestOut]:
    query = select(HelpRequest).where(HelpRequest.status == "open")
    if district:
        query = query.where(HelpRequest.district == district)
    rows = db.scalars(query.order_by(HelpRequest.time)).all()
    return [to_request_out(row) for row in rows]


@router.get("/requests/me", response_model=list[RequestOut])
def list_my_requests(role: str, user_id: str, db: Session = Depends(get_db)) -> list[RequestOut]:
    if role == "pensioner":
        rows = db.scalars(select(HelpRequest).where(HelpRequest.pensioner_id == user_id)).all()
    else:
        rows = db.scalars(select(HelpRequest).where(HelpRequest.volunteer_id == user_id)).all()
    return [to_request_out(row) for row in rows]


@router.post("/requests", response_model=RequestOut)
def create_request(payload: RequestCreateIn, db: Session = Depends(get_db)) -> RequestOut:
    request = HelpRequest(
        title=payload.title,
        help_type=payload.help_type,
        reward_coins=payload.reward_coins,
        district=payload.district,
        address=payload.address,
        time=payload.time,
        comment=payload.comment,
        pensioner_name=payload.pensioner_name,
        pensioner_id=payload.pensioner_id,
        status="open",
    )
    db.add(request)
    db.commit()
    db.refresh(request)
    return to_request_out(request)


@router.post("/requests/{request_id}/accept", response_model=RequestOut)
def accept_request(request_id: str, payload: RequestAcceptIn, db: Session = Depends(get_db)) -> RequestOut:
    request = db.get(HelpRequest, request_id)
    volunteer = db.get(User, payload.volunteer_id)
    if request is None or volunteer is None:
        raise HTTPException(status_code=404, detail="Заявка или волонтер не найдены")
    request.status = "accepted"
    request.volunteer_id = payload.volunteer_id
    request.volunteer_name = volunteer.full_name
    db.commit()
    db.refresh(request)
    return to_request_out(request)


@router.post("/requests/{request_id}/start", response_model=RequestOut)
def start_request(request_id: str, payload: RequestAcceptIn, db: Session = Depends(get_db)) -> RequestOut:
    request = db.get(HelpRequest, request_id)
    if request is None or request.volunteer_id != payload.volunteer_id:
        raise HTTPException(status_code=404, detail="Заявка не найдена")
    request.status = "inprogress"
    db.commit()
    db.refresh(request)
    return to_request_out(request)


@router.post("/requests/{request_id}/complete", response_model=RequestOut)
def complete_request(request_id: str, payload: RequestCompleteIn, db: Session = Depends(get_db)) -> RequestOut:
    request = db.get(HelpRequest, request_id)
    if request is None:
        raise HTTPException(status_code=404, detail="Заявка не найдена")
    request.status = "completed"
    request.rating = payload.rating
    db.add(
        WalletTransaction(
            user_id=request.volunteer_id,
            amount=request.reward_coins,
            reason=f"Выполнена заявка: {request.title}",
        )
    )
    db.commit()
    db.refresh(request)
    return to_request_out(request)


@router.get("/wallet/{user_id}", response_model=WalletOut)
def wallet(user_id: str, db: Session = Depends(get_db)) -> WalletOut:
    tx = db.scalars(select(WalletTransaction).where(WalletTransaction.user_id == user_id)).all()
    transactions = [
        {
            "id": i.id,
            "amount": i.amount,
            "reason": i.reason,
            "created_at": i.created_at,
        }
        for i in tx
    ]
    return WalletOut(balance=sum(i["amount"] for i in transactions), transactions=transactions)


@router.get("/rewards", response_model=list[RewardOut])
def rewards(db: Session = Depends(get_db)) -> list[RewardOut]:
    return db.scalars(select(Reward)).all()


@router.get("/leaderboard", response_model=list[LeaderboardOut])
def leaderboard(db: Session = Depends(get_db)) -> list[LeaderboardOut]:
    rows = db.execute(
        select(
            User.full_name.label("name"),
            User.district.label("district"),
            func.coalesce(func.sum(WalletTransaction.amount), 0).label("coins"),
        )
        .join(WalletTransaction, WalletTransaction.user_id == User.id, isouter=True)
        .where(User.role == "volunteer")
        .group_by(User.id)
        .order_by(func.coalesce(func.sum(WalletTransaction.amount), 0).desc())
    ).all()
    result = []
    for row in rows:
        coins = int(row.coins or 0)
        result.append(
            LeaderboardOut(
                volunteer_name=row.name,
                district=row.district,
                coins=coins,
                rank_title=resolve_rank(coins),
                badges=[{"id": "base", "title": "Активный участник"}],
            )
        )
    return result


@router.get("/profiles/pensioner/{user_id}", response_model=ProfileOut)
def pensioner_profile(user_id: str, db: Session = Depends(get_db)) -> ProfileOut:
    return build_profile("pensioner", user_id, db)


@router.get("/profiles/volunteer/{user_id}", response_model=ProfileOut)
def volunteer_profile(user_id: str, db: Session = Depends(get_db)) -> ProfileOut:
    return build_profile("volunteer", user_id, db)


def build_profile(role: str, user_id: str, db: Session) -> ProfileOut:
    user = db.get(User, user_id)
    if user is None:
        raise HTTPException(status_code=404, detail="Пользователь не найден")
    if role == "pensioner":
        active = db.scalar(
            select(func.count()).select_from(HelpRequest).where(
                HelpRequest.pensioner_id == user_id, HelpRequest.status != "completed"
            )
        )
        done = db.scalar(
            select(func.count()).select_from(HelpRequest).where(
                HelpRequest.pensioner_id == user_id, HelpRequest.status == "completed"
            )
        )
    else:
        active = db.scalar(
            select(func.count()).select_from(HelpRequest).where(
                HelpRequest.volunteer_id == user_id,
                HelpRequest.status.in_(["accepted", "inprogress"]),
            )
        )
        done = db.scalar(
            select(func.count()).select_from(HelpRequest).where(
                HelpRequest.volunteer_id == user_id, HelpRequest.status == "completed"
            )
        )
    return ProfileOut(
        user_id=user.id,
        full_name=user.full_name,
        role=user.role,
        active_requests=int(active or 0),
        completed_requests=int(done or 0),
    )


def resolve_rank(coins: int) -> str:
    if coins >= 1000:
        return "Герой района"
    if coins >= 500:
        return "Наставник"
    return "Новичок добра"


def to_request_out(row: HelpRequest) -> RequestOut:
    return RequestOut(
        id=row.id,
        title=row.title,
        help_type=row.help_type,
        reward_coins=row.reward_coins,
        district=row.district,
        address=row.address,
        time=row.time,
        comment=row.comment,
        pensioner_name=row.pensioner_name,
        pensioner_id=row.pensioner_id,
        status=row.status,
        volunteer_name=row.volunteer_name,
        volunteer_id=row.volunteer_id,
        rating=row.rating,
        lat=row.lat,
        lon=row.lon,
    )

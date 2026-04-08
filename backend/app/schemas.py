from datetime import datetime
from pydantic import BaseModel, Field


class RoleSelectIn(BaseModel):
    role: str = Field(pattern="^(pensioner|volunteer)$")
    full_name: str = Field(min_length=2, max_length=120)


class AuthOut(BaseModel):
    user_id: str
    role: str
    full_name: str
    token: str


class RequestCreateIn(BaseModel):
    title: str
    help_type: str
    reward_coins: int = Field(ge=1, le=1000)
    district: str
    address: str
    time: str
    comment: str
    pensioner_id: str
    pensioner_name: str


class RequestAcceptIn(BaseModel):
    volunteer_id: str


class RequestCompleteIn(BaseModel):
    rating: int = Field(ge=1, le=5)


class RequestOut(BaseModel):
    id: str
    title: str
    help_type: str
    reward_coins: int
    district: str
    address: str
    time: str
    comment: str
    pensioner_name: str
    pensioner_id: str
    status: str
    volunteer_name: str | None = None
    volunteer_id: str | None = None
    rating: int | None = None
    lat: float | None = None
    lon: float | None = None


class TransactionOut(BaseModel):
    id: str
    amount: int
    reason: str
    created_at: datetime


class WalletOut(BaseModel):
    balance: int
    transactions: list[TransactionOut]


class RewardOut(BaseModel):
    id: str
    title: str
    category: str
    cost: int


class BadgeOut(BaseModel):
    id: str
    title: str


class LeaderboardOut(BaseModel):
    volunteer_name: str
    district: str
    coins: int
    rank_title: str
    badges: list[BadgeOut]


class ProfileOut(BaseModel):
    user_id: str
    full_name: str
    role: str
    active_requests: int
    completed_requests: int

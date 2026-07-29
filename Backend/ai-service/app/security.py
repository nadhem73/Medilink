import jwt
from fastapi import Depends, HTTPException
from fastapi.security import HTTPBearer
from app.config import settings

security = HTTPBearer()

ALGORITHM = "HS384"  # force reload


def verify_jwt(token: str) -> dict:
    try:
        return jwt.decode(token, settings.JWT_SECRET, algorithms=[ALGORITHM])
    except jwt.ExpiredSignatureError:
        raise HTTPException(status_code=401, detail="Token expiré")
    except jwt.InvalidTokenError:
        raise HTTPException(status_code=401, detail="Token invalide")


async def get_current_user(credentials=Depends(security)) -> dict:
    payload = verify_jwt(credentials.credentials)
    return {
        "userId": payload.get("userId"),
        "email": payload.get("sub"),
        "role": payload.get("role"),
    }

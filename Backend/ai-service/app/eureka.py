import logging
import py_eureka_client.eureka_client as eureka_client
from app.config import settings

logger = logging.getLogger(__name__)


async def register_with_eureka():
    try:
        await eureka_client.init_async(
            eureka_server=settings.EUREKA_SERVER,
            app_name=settings.EUREKA_APP_NAME,
            instance_port=settings.EUREKA_INSTANCE_PORT,
            instance_host=settings.EUREKA_INSTANCE_HOST,
            renewal_interval_in_secs=10,
            duration_in_secs=20,
        )
        logger.info("Registered with Eureka: %s", settings.EUREKA_APP_NAME)
    except Exception as e:
        logger.warning("Eureka registration failed (non-blocking): %s", e)

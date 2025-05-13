-- Khởi tạo database nếu chưa tồn tại
CREATE DATABASE IF NOT EXISTS "NihongoIT";

-- Đảm bảo database đang được sử dụng
\c "NihongoIT";

-- Tạo extension nếu cần
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Kiểm tra xem schema public đã tồn tại chưa
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_namespace WHERE nspname = 'public') THEN
        CREATE SCHEMA public;
    END IF;
END
$$; 
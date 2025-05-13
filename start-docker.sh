#!/bin/bash

# Script để thiết lập và khởi động docker-compose

# Đảm bảo rằng biến môi trường được đặt
if [ -f .env ]; then
    echo "Tải biến môi trường từ file .env..."
    export $(grep -v '^#' .env | xargs)
else
    echo "Cảnh báo: File .env không tồn tại, sử dụng giá trị mặc định"
fi

# Hiển thị menu
echo "===== Nihongo IT Docker Setup ====="
echo "1. Chỉ khởi động PostgreSQL và PgAdmin"
echo "2. Khởi động tất cả dịch vụ (cần uncomment trong docker-compose.yaml)"
echo "3. Dừng tất cả dịch vụ"
echo "4. Xóa tất cả dịch vụ và volumes"
echo "5. Xem logs"
echo "0. Thoát"

read -p "Nhập lựa chọn của bạn: " choice

case $choice in
    1)
        echo "Khởi động PostgreSQL và PgAdmin..."
        docker-compose up -d
        ;;
    2)
        echo "Khởi động tất cả dịch vụ..."
        docker-compose up -d --build
        ;;
    3)
        echo "Dừng tất cả dịch vụ..."
        docker-compose down
        ;;
    4)
        echo "Xóa tất cả dịch vụ và volumes..."
        docker-compose down -v
        ;;
    5)
        echo "Xem logs..."
        docker-compose logs -f
        ;;
    0)
        echo "Thoát"
        exit 0
        ;;
    *)
        echo "Lựa chọn không hợp lệ"
        exit 1
        ;;
esac

echo "Hoàn tất!" 
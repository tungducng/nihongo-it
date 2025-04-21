-- Additional vocabulary entries for IT Japanese learning
-- This file adds 30 new vocabulary items across different topics

-- Programming Languages vocabulary
WITH topic AS (SELECT topic_id FROM topics WHERE name = 'プログラミング言語')
INSERT INTO vocabulary (vocab_id, term, meaning, pronunciation, example, example_meaning, jlpt_level, topic_id, created_at)
VALUES
  (gen_random_uuid(), 'コンパイル', 'Biên dịch', 'こんぱいる', 'ソースコードをコンパイルする', 'Biên dịch mã nguồn', 'N2', (SELECT topic_id FROM topic), NOW()),
  (gen_random_uuid(), '実行', 'Thực thi', 'じっこう', 'プログラムを実行する', 'Chạy chương trình', 'N3', (SELECT topic_id FROM topic), NOW()),
  (gen_random_uuid(), 'デバッグ', 'Gỡ lỗi', 'でばっぐ', 'コードをデバッグする', 'Gỡ lỗi code', 'N2', (SELECT topic_id FROM topic), NOW()),
  (gen_random_uuid(), '配列', 'Mảng', 'はいれつ', '配列を初期化する', 'Khởi tạo mảng', 'N3', (SELECT topic_id FROM topic), NOW()),
  (gen_random_uuid(), 'オブジェクト', 'Đối tượng', 'おぶじぇくと', 'オブジェクトを生成する', 'Tạo đối tượng', 'N3', (SELECT topic_id FROM topic), NOW())
ON CONFLICT DO NOTHING;

-- Algorithm and Data Structure vocabulary
WITH topic AS (SELECT topic_id FROM topics WHERE name = 'アルゴリズムとデータ構造')
INSERT INTO vocabulary (vocab_id, term, meaning, pronunciation, example, example_meaning, jlpt_level, topic_id, created_at)
VALUES
  (gen_random_uuid(), 'ソート', 'Sắp xếp', 'そーと', 'データをソートする', 'Sắp xếp dữ liệu', 'N3', (SELECT topic_id FROM topic), NOW()),
  (gen_random_uuid(), '二分探索', 'Tìm kiếm nhị phân', 'にぶんたんさく', '二分探索を実装する', 'Triển khai tìm kiếm nhị phân', 'N1', (SELECT topic_id FROM topic), NOW()),
  (gen_random_uuid(), 'リンクリスト', 'Danh sách liên kết', 'りんくりすと', 'リンクリストを操作する', 'Thao tác với danh sách liên kết', 'N2', (SELECT topic_id FROM topic), NOW()),
  (gen_random_uuid(), '再帰', 'Đệ quy', 'さいき', '再帰関数を書く', 'Viết hàm đệ quy', 'N2', (SELECT topic_id FROM topic), NOW()),
  (gen_random_uuid(), '計算量', 'Độ phức tạp tính toán', 'けいさんりょう', 'アルゴリズムの計算量を分析する', 'Phân tích độ phức tạp của thuật toán', 'N1', (SELECT topic_id FROM topic), NOW())
ON CONFLICT DO NOTHING;

-- Frontend vocabulary
WITH topic AS (SELECT topic_id FROM topics WHERE name = 'フロントエンド')
INSERT INTO vocabulary (vocab_id, term, meaning, pronunciation, example, example_meaning, jlpt_level, topic_id, created_at)
VALUES
  (gen_random_uuid(), 'ユーザーインターフェース', 'Giao diện người dùng', 'ゆーざーいんたーふぇーす', 'ユーザーインターフェースを設計する', 'Thiết kế giao diện người dùng', 'N2', (SELECT topic_id FROM topic), NOW()),
  (gen_random_uuid(), 'レンダリング', 'Hiển thị', 'れんだりんぐ', 'コンポーネントをレンダリングする', 'Hiển thị component', 'N2', (SELECT topic_id FROM topic), NOW()),
  (gen_random_uuid(), 'イベントリスナー', 'Lắng nghe sự kiện', 'いべんとりすなー', 'イベントリスナーを追加する', 'Thêm trình lắng nghe sự kiện', 'N2', (SELECT topic_id FROM topic), NOW()),
  (gen_random_uuid(), 'スタイルシート', 'Bảng định kiểu', 'すたいるしーと', 'スタイルシートを適用する', 'Áp dụng bảng định kiểu', 'N2', (SELECT topic_id FROM topic), NOW()),
  (gen_random_uuid(), '単一ページアプリケーション', 'Ứng dụng một trang', 'たんいつぺーじあぷりけーしょん', '単一ページアプリケーションを開発する', 'Phát triển ứng dụng một trang', 'N1', (SELECT topic_id FROM topic), NOW())
ON CONFLICT DO NOTHING;

-- Backend vocabulary
WITH topic AS (SELECT topic_id FROM topics WHERE name = 'バックエンド')
INSERT INTO vocabulary (vocab_id, term, meaning, pronunciation, example, example_meaning, jlpt_level, topic_id, created_at)
VALUES
  (gen_random_uuid(), 'サーバー', 'Máy chủ', 'さーばー', 'サーバーを構築する', 'Xây dựng máy chủ', 'N3', (SELECT topic_id FROM topic), NOW()),
  (gen_random_uuid(), 'エンドポイント', 'Điểm cuối', 'えんどぽいんと', 'APIエンドポイントを作成する', 'Tạo điểm cuối API', 'N2', (SELECT topic_id FROM topic), NOW()),
  (gen_random_uuid(), '認証', 'Xác thực', 'にんしょう', 'ユーザー認証を実装する', 'Triển khai xác thực người dùng', 'N2', (SELECT topic_id FROM topic), NOW()),
  (gen_random_uuid(), 'マイクロサービス', 'Vi dịch vụ', 'まいくろさーびす', 'マイクロサービスアーキテクチャを採用する', 'Áp dụng kiến trúc vi dịch vụ', 'N1', (SELECT topic_id FROM topic), NOW()),
  (gen_random_uuid(), 'ミドルウェア', 'Phần mềm trung gian', 'みどるうぇあ', 'ミドルウェアを追加する', 'Thêm phần mềm trung gian', 'N2', (SELECT topic_id FROM topic), NOW())
ON CONFLICT DO NOTHING;

-- Database vocabulary
WITH topic AS (SELECT topic_id FROM topics WHERE name = 'SQLの基本')
INSERT INTO vocabulary (vocab_id, term, meaning, pronunciation, example, example_meaning, jlpt_level, topic_id, created_at)
VALUES
  (gen_random_uuid(), 'クエリ', 'Truy vấn', 'くえり', 'SQLクエリを実行する', 'Thực thi truy vấn SQL', 'N3', (SELECT topic_id FROM topic), NOW()),
  (gen_random_uuid(), '結合', 'Kết hợp', 'けつごう', 'テーブルを結合する', 'Kết hợp các bảng', 'N2', (SELECT topic_id FROM topic), NOW()),
  (gen_random_uuid(), 'インデックス', 'Chỉ mục', 'いんでっくす', 'インデックスを作成する', 'Tạo chỉ mục', 'N2', (SELECT topic_id FROM topic), NOW()),
  (gen_random_uuid(), 'トランザクション', 'Giao dịch', 'とらんざくしょん', 'トランザクションを開始する', 'Bắt đầu giao dịch', 'N1', (SELECT topic_id FROM topic), NOW()),
  (gen_random_uuid(), '正規化', 'Chuẩn hóa', 'せいきか', 'データベースを正規化する', 'Chuẩn hóa cơ sở dữ liệu', 'N1', (SELECT topic_id FROM topic), NOW())
ON CONFLICT DO NOTHING;

-- AI and Machine Learning vocabulary
WITH topic AS (SELECT topic_id FROM topics WHERE name = '機械学習')
INSERT INTO vocabulary (vocab_id, term, meaning, pronunciation, example, example_meaning, jlpt_level, topic_id, created_at)
VALUES
  (gen_random_uuid(), '学習モデル', 'Mô hình học tập', 'がくしゅうもでる', '学習モデルを訓練する', 'Huấn luyện mô hình học tập', 'N1', (SELECT topic_id FROM topic), NOW()),
  (gen_random_uuid(), '予測', 'Dự đoán', 'よそく', 'データを予測する', 'Dự đoán dữ liệu', 'N3', (SELECT topic_id FROM topic), NOW()),
  (gen_random_uuid(), '特徴抽出', 'Trích xuất đặc trưng', 'とくちょうちゅうしゅつ', '特徴抽出を行う', 'Thực hiện trích xuất đặc trưng', 'N1', (SELECT topic_id FROM topic), NOW()),
  (gen_random_uuid(), '過学習', 'Quá khớp', 'かがくしゅう', '過学習を防ぐ', 'Ngăn chặn quá khớp', 'N1', (SELECT topic_id FROM topic), NOW()),
  (gen_random_uuid(), 'ニューラルネットワーク', 'Mạng nơ-ron', 'にゅーらるねっとわーく', 'ニューラルネットワークを構築する', 'Xây dựng mạng nơ-ron', 'N1', (SELECT topic_id FROM topic), NOW())
ON CONFLICT DO NOTHING;

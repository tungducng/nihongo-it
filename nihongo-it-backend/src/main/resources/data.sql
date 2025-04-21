-- Insert default roles if they don't exist
INSERT INTO roles (role_id, role_name)
VALUES (1, 'ROLE_ADMIN')
ON CONFLICT (role_id) DO NOTHING;

INSERT INTO roles (role_id, role_name)
VALUES (2, 'ROLE_USER')
ON CONFLICT (role_id) DO NOTHING;

-- Insert categories if they don't exist
INSERT INTO categories (category_id, name, meaning, display_order, created_at, updated_at)
VALUES
  (gen_random_uuid(), 'IT基礎', 'Cơ bản về IT', 1, NOW(), NOW()),
  (gen_random_uuid(), 'プログラミング', 'Lập trình (Programming)', 2, NOW(), NOW()),
  (gen_random_uuid(), 'ウェブ開発', 'Phát triển Web', 3, NOW(), NOW()),
  (gen_random_uuid(), 'データベース', 'Cơ sở dữ liệu (Database)', 4, NOW(), NOW()),
  (gen_random_uuid(), '人工知能・データ', 'Trí tuệ nhân tạo / Dữ liệu', 5, NOW(), NOW()),
  (gen_random_uuid(), 'コミュニケーション', 'Giao tiếp & teamwork', 6, NOW(), NOW()),
  (gen_random_uuid(), '実務とキャリア', 'Dự án & thực tế', 7, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

-- Insert topics if they don't exist (IT Basics)
WITH category AS (SELECT category_id FROM categories WHERE name = 'IT基礎')
INSERT INTO topics (topic_id, name, meaning, display_order, category_id, created_at, updated_at)
VALUES
  (gen_random_uuid(), 'コンピューターの構造', 'Kiến trúc máy tính', 1, (SELECT category_id FROM category), NOW(), NOW()),
  (gen_random_uuid(), 'オペレーティングシステム', 'Hệ điều hành', 2, (SELECT category_id FROM category), NOW(), NOW()),
  (gen_random_uuid(), 'ハードウェア', 'Phần cứng', 3, (SELECT category_id FROM category), NOW(), NOW()),
  (gen_random_uuid(), 'ソフトウェア', 'Phần mềm', 4, (SELECT category_id FROM category), NOW(), NOW()),
  (gen_random_uuid(), 'コンピューターネットワーク', 'Mạng máy tính', 5, (SELECT category_id FROM category), NOW(), NOW()),
  (gen_random_uuid(), 'IT共通用語', 'Thuật ngữ chung trong IT', 6, (SELECT category_id FROM category), NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

-- Insert topics if they don't exist (Programming)
WITH category AS (SELECT category_id FROM categories WHERE name = 'プログラミング')
INSERT INTO topics (topic_id, name, meaning, display_order, category_id, created_at, updated_at)
VALUES
  (gen_random_uuid(), 'プログラミング言語', 'Ngôn ngữ lập trình', 1, (SELECT category_id FROM category), NOW(), NOW()),
  (gen_random_uuid(), 'アルゴリズムとデータ構造', 'Thuật toán & cấu trúc dữ liệu', 2, (SELECT category_id FROM category), NOW(), NOW()),
  (gen_random_uuid(), 'デバッグとエラーハンドリング', 'Gỡ lỗi và xử lý lỗi', 3, (SELECT category_id FROM category), NOW(), NOW()),
  (gen_random_uuid(), 'オブジェクト指向', 'Hướng đối tượng', 4, (SELECT category_id FROM category), NOW(), NOW()),
  (gen_random_uuid(), 'コマンドライン・ターミナル', 'Command line & terminal', 5, (SELECT category_id FROM category), NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

-- Insert topics if they don't exist (Web Development)
WITH category AS (SELECT category_id FROM categories WHERE name = 'ウェブ開発')
INSERT INTO topics (topic_id, name, meaning, display_order, category_id, created_at, updated_at)
VALUES
  (gen_random_uuid(), 'フロントエンド', 'Frontend', 1, (SELECT category_id FROM category), NOW(), NOW()),
  (gen_random_uuid(), 'バックエンド', 'Backend', 2, (SELECT category_id FROM category), NOW(), NOW()),
  (gen_random_uuid(), 'システムアーキテクチャ', 'Kiến trúc hệ thống', 3, (SELECT category_id FROM category), NOW(), NOW()),
  (gen_random_uuid(), 'DevOps・CI/CD', 'DevOps, CI/CD', 4, (SELECT category_id FROM category), NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

-- Insert topics if they don't exist (Database)
WITH category AS (SELECT category_id FROM categories WHERE name = 'データベース')
INSERT INTO topics (topic_id, name, meaning, display_order, category_id, created_at, updated_at)
VALUES
  (gen_random_uuid(), 'SQLの基本', 'SQL cơ bản', 1, (SELECT category_id FROM category), NOW(), NOW()),
  (gen_random_uuid(), 'データベース設計', 'Thiết kế cơ sở dữ liệu', 2, (SELECT category_id FROM category), NOW(), NOW()),
  (gen_random_uuid(), 'NoSQL・新しい概念', 'NoSQL và các khái niệm mới', 3, (SELECT category_id FROM category), NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

-- Insert topics if they don't exist (AI/Data)
WITH category AS (SELECT category_id FROM categories WHERE name = '人工知能・データ')
INSERT INTO topics (topic_id, name, meaning, display_order, category_id, created_at, updated_at)
VALUES
  (gen_random_uuid(), '機械学習', 'Học máy', 1, (SELECT category_id FROM category), NOW(), NOW()),
  (gen_random_uuid(), '人工知能とディープラーニング', 'AI & Deep Learning', 2, (SELECT category_id FROM category), NOW(), NOW()),
  (gen_random_uuid(), '自然言語処理', 'Xử lý ngôn ngữ tự nhiên', 3, (SELECT category_id FROM category), NOW(), NOW()),
  (gen_random_uuid(), 'データサイエンス', 'Khoa học dữ liệu', 4, (SELECT category_id FROM category), NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

-- Insert topics if they don't exist (Communication)
WITH category AS (SELECT category_id FROM categories WHERE name = 'コミュニケーション')
INSERT INTO topics (topic_id, name, meaning, display_order, category_id, created_at, updated_at)
VALUES
  (gen_random_uuid(), 'プロジェクトの会話用語', 'Thuật ngữ giao tiếp trong dự án', 1, (SELECT category_id FROM category), NOW(), NOW()),
  (gen_random_uuid(), 'アジャイル・スクラム開発', 'Quy trình làm việc agile/scrum', 2, (SELECT category_id FROM category), NOW(), NOW()),
  (gen_random_uuid(), 'ドキュメント・コメント', 'Đọc hiểu tài liệu, comment code', 3, (SELECT category_id FROM category), NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

-- Insert topics if they don't exist (Work & Career)
WITH category AS (SELECT category_id FROM categories WHERE name = '実務とキャリア')
INSERT INTO topics (topic_id, name, meaning, display_order, category_id, created_at, updated_at)
VALUES
  (gen_random_uuid(), 'IT面接', 'Phỏng vấn xin việc IT', 1, (SELECT category_id FROM category), NOW(), NOW()),
  (gen_random_uuid(), '自己紹介・履歴書', 'CV, giới thiệu bản thân', 2, (SELECT category_id FROM category), NOW(), NOW()),
  (gen_random_uuid(), '実務で使う単語', 'Từ vựng thực hành/thường dùng khi làm việc', 3, (SELECT category_id FROM category), NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

-- Insert sample vocabulary items for programming topic
WITH topic AS (SELECT topic_id FROM topics WHERE name = 'プログラミング言語')
INSERT INTO vocabulary (vocab_id, term, meaning, pronunciation, example, example_meaning, jlpt_level, topic_id, created_at)
VALUES
  (gen_random_uuid(), '変数', 'Biến', 'へんすう', '変数を宣言する', 'Khai báo biến', 'N4', (SELECT topic_id FROM topic), NOW()),
  (gen_random_uuid(), '関数', 'Hàm', 'かんすう', '関数を呼び出す', 'Gọi hàm', 'N3', (SELECT topic_id FROM topic), NOW())
ON CONFLICT DO NOTHING;

-- Insert sample vocabulary items for web development topic
WITH topic AS (SELECT topic_id FROM topics WHERE name = 'フロントエンド')
INSERT INTO vocabulary (vocab_id, term, meaning, pronunciation, example, example_meaning, jlpt_level, topic_id, created_at)
VALUES
  (gen_random_uuid(), 'ウェブサイト', 'Trang web', NULL, 'ウェブサイトを構築する', 'Xây dựng trang web', 'N5', (SELECT topic_id FROM topic), NOW()),
  (gen_random_uuid(), 'レスポンシブデザイン', 'Thiết kế đáp ứng', NULL, 'レスポンシブデザインを実装する', 'Triển khai thiết kế đáp ứng', 'N2', (SELECT topic_id FROM topic), NOW())
ON CONFLICT DO NOTHING;
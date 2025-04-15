-- Insert default roles if they don't exist
INSERT INTO roles (role_id, role_name)
VALUES (1, 'ROLE_ADMIN')
ON CONFLICT (role_id) DO NOTHING;

INSERT INTO roles (role_id, role_name)
VALUES (2, 'ROLE_USER')
ON CONFLICT (role_id) DO NOTHING;

INSERT INTO vocabulary (vocab_id, hiragana, meaning, kanji, katakana, example_sentence, example_sentence_translation, category, jlpt_level) VALUES
(gen_random_uuid(), 'せんもんご', 'Thuật ngữ chuyên môn', '専門語', NULL, 'プログラミングの専門語を学ぶことは大切です。', 'Việc học thuật ngữ chuyên môn lập trình là quan trọng.', 'Programming', 'N5'),
(gen_random_uuid(), NULL, 'Cơ sở dữ liệu', NULL, 'データベース', 'このデータベースは大量のデータを保存できます。', 'Cơ sở dữ liệu này có thể lưu trữ một lượng lớn dữ liệu.', 'Database', 'N4'),
(gen_random_uuid(), 'うぇぶかいはつ', 'Phát triển web', 'ウェブ開発', NULL, 'ウェブ開発の技術は日々進化しています。', 'Công nghệ phát triển web đang phát triển hàng ngày.', 'Web Development', 'N3'),
(gen_random_uuid(), 'じんこうちのう', 'Trí tuệ nhân tạo', '人工知能', NULL, '人工知能の発展は現代社会に大きな影響を与えています。', 'Sự phát triển của trí tuệ nhân tạo đang có tác động lớn đến xã hội hiện đại.', 'AI/Machine Learning', 'N2'),
(gen_random_uuid(), 'じょうほうあんぜん', 'An ninh thông tin', '情報安全', NULL, '情報安全を確保するために、定期的にパスワードを変更してください。', 'Vui lòng thay đổi mật khẩu thường xuyên để đảm bảo an ninh thông tin.', 'Cyber Security', 'N1'),
(gen_random_uuid(), NULL, 'Công nghệ', NULL, 'テクノロジー', '現代のテクノロジーは私たちの生活を変えました。', 'Công nghệ hiện đại đã thay đổi cuộc sống của chúng ta.', 'Technology', 'N5'),
(gen_random_uuid(), NULL, 'Phần cứng máy tính', NULL, 'ハードウェア', '新しいハードウェアを導入する前に、互換性を確認する必要があります。', 'Trước khi cài đặt phần cứng mới, bạn cần kiểm tra tính tương thích.', 'Computer Hardware', 'N4'),
(gen_random_uuid(), NULL, 'Mạng lưới', NULL, 'ネットワーク', '安定したネットワーク接続は重要です。', 'Kết nối mạng ổn định là rất quan trọng.', 'Networking', 'N3'),
(gen_random_uuid(), 'そふとうぇあてすと', 'Kiểm thử phần mềm', 'ソフトウェアテスト', NULL, 'ソフトウェアテストはプロジェクトの重要な部分です。', 'Kiểm thử phần mềm là một phần quan trọng của dự án.', 'Software Testing', 'N2'),
(gen_random_uuid(), 'ぷろじぇくとかんり', 'Quản lý dự án', 'プロジェクト管理', NULL, '効果的なプロジェクト管理は成功への鍵です。', 'Quản lý dự án hiệu quả là chìa khóa để thành công.', 'Project Management', 'N1');
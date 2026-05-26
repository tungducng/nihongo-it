/* Mock data for the user UI kit. Inspired by the IT-Japanese learning context:
   real Japanese terms an engineer would meet on the job, with Vietnamese meanings. */

const VOCAB = [
  { id: '1',  term: '会議',     pron: 'かいぎ',         meaning: 'cuộc họp — buổi gặp mặt giữa nhiều người để thảo luận', jlpt: 'N3', topic: 'Công sở', saved: true },
  { id: '2',  term: '仕様書',   pron: 'しようしょ',     meaning: 'tài liệu đặc tả — mô tả chi tiết yêu cầu kỹ thuật của hệ thống', jlpt: 'N1', topic: 'Lập trình', saved: false },
  { id: '3',  term: '納期',     pron: 'のうき',         meaning: 'thời hạn giao hàng / deadline dự án', jlpt: 'N2', topic: 'Dự án', saved: true },
  { id: '4',  term: '不具合',   pron: 'ふぐあい',       meaning: 'lỗi / bug — sự cố không hoạt động đúng', jlpt: 'N2', topic: 'QA', saved: false },
  { id: '5',  term: '実装',     pron: 'じっそう',       meaning: 'triển khai / implement — viết code cho một tính năng', jlpt: 'N2', topic: 'Lập trình', saved: false },
  { id: '6',  term: '要件定義', pron: 'ようけんていぎ', meaning: 'xác định yêu cầu — bước đầu trong dự án', jlpt: 'N1', topic: 'Dự án', saved: true },
  { id: '7',  term: '検収',     pron: 'けんしゅう',     meaning: 'nghiệm thu — kiểm tra và chấp nhận bàn giao', jlpt: 'N1', topic: 'Dự án', saved: false },
  { id: '8',  term: '画面',     pron: 'がめん',         meaning: 'màn hình — UI một trang/cửa sổ', jlpt: 'N4', topic: 'UI/UX', saved: false },
  { id: '9',  term: '修正',     pron: 'しゅうせい',     meaning: 'sửa đổi / fix — chỉnh sửa cho đúng', jlpt: 'N3', topic: 'QA', saved: false },
  { id: '10', term: '本番環境', pron: 'ほんばんかんきょう', meaning: 'môi trường production', jlpt: 'N2', topic: 'DevOps', saved: false },
  { id: '11', term: '依頼',     pron: 'いらい',         meaning: 'yêu cầu / đề nghị (lịch sự)', jlpt: 'N3', topic: 'Công sở', saved: false },
  { id: '12', term: '対応',     pron: 'たいおう',       meaning: 'xử lý / hỗ trợ / ứng phó', jlpt: 'N3', topic: 'Công sở', saved: true },
];

const CONVERSATIONS = [
  { id: 'c1', title: 'Báo cáo tiến độ dự án trong cuộc họp hàng tuần',
    desc: 'Mẫu hội thoại giữa Project Manager và developer về tiến độ sprint.', jlpt: 'N3', unit: 7 },
  { id: 'c2', title: 'Phỏng vấn xin việc kỹ sư cầu nối (BrSE)',
    desc: 'Tự giới thiệu, mô tả kinh nghiệm và trả lời câu hỏi phỏng vấn.', jlpt: 'N4', unit: 12 },
  { id: 'c3', title: 'Trao đổi với khách hàng về thay đổi yêu cầu',
    desc: 'Lịch sự đề xuất impact của change request lên timeline.', jlpt: 'N2', unit: 18 },
  { id: 'c4', title: 'Daily stand-up meeting kiểu Nhật',
    desc: 'Cách báo cáo 3 phần: hôm qua, hôm nay, và blockers.', jlpt: 'N4', unit: 3 },
  { id: 'c5', title: 'Báo cáo bug khẩn cấp trên production',
    desc: 'Thông báo sự cố, mô tả triệu chứng, đề xuất giải pháp tạm thời.', jlpt: 'N2', unit: 14 },
  { id: 'c6', title: 'Họp code review nội bộ',
    desc: 'Đưa ra góp ý xây dựng về code của đồng nghiệp một cách lịch sự.', jlpt: 'N3', unit: 9 },
];

const TOPICS = ['Tất cả', 'Công sở', 'Lập trình', 'QA', 'Dự án', 'UI/UX', 'DevOps'];

window.MOCK = { VOCAB, CONVERSATIONS, TOPICS };

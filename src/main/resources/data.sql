-- simple_data.sql - H2 veritabanı için basit versiyon

-- Questions tablosuna veri ekliyoruz
INSERT INTO questions (question_text, order_index, is_active) VALUES
('Araçta dış hasar var mı?', 1, true),
('Motor ile ilgili sorun var mı?', 2, true),
('Lastiklerde aşınma var mı?', 3, true);

-- Inspections tablosuna veri ekliyoruz
INSERT INTO inspections (car_id, inspection_date, status) VALUES
('ABC123', TIMESTAMP '2024-01-15 10:30:00', 'COMPLETED'),
('ABC123', TIMESTAMP '2024-01-15 10:40:00', 'IN_PROGRESS'),
('XYZ789', TIMESTAMP '2024-01-10 14:15:00', 'COMPLETED');

-- inspection_answers tablosuna veri ekliyoruz
INSERT INTO inspection_answers (inspection_id, question_id, answer, description) VALUES
(1, 1, 'YES', 'Sol kapıda çizik mevcut'),
(1, 2, 'NO', NULL),
(1, 3, 'YES', 'Ön lastiklerde hafif aşınma'),
(2, 1, 'NO', NULL),
(2, 2, 'YES', 'Motor yağı sızıntısı');

-- inspection_photos tablosuna veri ekliyoruz
INSERT INTO inspection_photos (answer_id, photo_url, is_new) VALUES
(1, 'https://example-cloud.com/photos/abc123-door-scratch-1.jpg', false),
(1, 'https://example-cloud.com/photos/abc123-door-scratch-2.jpg', false),
(3, 'https://example-cloud.com/photos/abc123-tire-wear-1.jpg', false),
(5, 'https://example-cloud.com/photos/xyz789-engine-leak-1.jpg', false),
(5, 'https://example-cloud.com/photos/xyz789-engine-leak-2.jpg', false);
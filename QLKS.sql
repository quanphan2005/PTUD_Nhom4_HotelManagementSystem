create database QLKS;
use QLKS;

-- Create LoaiPhong table
CREATE TABLE LoaiPhong (
    ma_loai_phong CHAR(11) PRIMARY KEY,
    ten_loai_phong NVARCHAR(255) not null,
    so_luong_khach int not null,
    phan_loai NVARCHAR(255) not null,
	thoi_gian_tao datetime default getdate(),
	da_xoa bit default 0,
);

-- Create Phong table
CREATE TABLE Phong (
    ma_phong CHAR(11) PRIMARY KEY,
    ten_phong NVARCHAR(255) not null,
    dang_hoat_dong BIT default 1,
    ghi_chu NVARCHAR(255),
    mo_ta_phong NVARCHAR(255),
    ma_loai_phong CHAR(11),
	thoi_gian_tao datetime default getdate(),
	da_xoa bit default 0,
    FOREIGN KEY (ma_loai_phong) REFERENCES LoaiPhong(ma_loai_phong)
);

create table CongViec(
    ma_cong_viec CHAR(11) PRIMARY KEY,
	ten_trang_thai NVARCHAR(255) not null, 
	tg_bat_dau datetime default getdate(),
	tg_ket_thuc datetime,
    ma_phong CHAR(11),
	thoi_gian_tao datetime default getdate(),
	da_xoa bit default 0,
	FOREIGN KEY (ma_phong) REFERENCES Phong(ma_phong)
) -- Chua

-- Create NoiThat table
CREATE TABLE NoiThat (
    ma_noi_that CHAR(11) PRIMARY KEY,
    ten_noi_that NVARCHAR(255) not null,
    mo_ta NVARCHAR(255),
	thoi_gian_tao datetime default getdate(),
	da_xoa bit default 0,
); -- CHUA CO DATA

-- Create Employee table
CREATE TABLE NhanVien (
    ma_nhan_vien CHAR(11) PRIMARY KEY,
    ten_nhan_vien NVARCHAR(255) not null,
    CCCD NVARCHAR(20) not null,
    ngay_sinh DATETIME,
	so_dien_thoai varchar(255),
	thoi_gian_tao datetime default getdate(),
	da_xoa bit default 0,
);

create table ChucVu(
	ma_chuc_vu CHAR(11) PRIMARY KEY,
    ten_chuc_vu NVARCHAR(255) not null,
	mo_ta NVARCHAR(255),
	thoi_gian_tao datetime default getdate(),
	da_xoa bit default 0,
)

-- Create TaiKhoan table
CREATE TABLE TaiKhoan (
    ma_tai_khoan CHAR(11) PRIMARY KEY,
    ten_dang_nhap NVARCHAR(255) not null,
    mat_khau NVARCHAR(255) not null,
    ma_chuc_vu CHAR(11),
    ma_nhan_vien CHAR(11),
	thoi_gian_tao datetime default getdate(),
	da_xoa bit default 0,
    FOREIGN KEY (ma_nhan_vien) REFERENCES NhanVien(ma_nhan_vien),
	FOREIGN KEY (ma_chuc_vu) REFERENCES ChucVu(ma_chuc_vu)
);


-- Create StaffAssignment table
CREATE TABLE PhienDangNhap (
    ma_phien_dang_nhap CHAR(11) PRIMARY KEY,
    so_quay int default 1,
    tg_bat_dau DATETIME not null,
    tg_ket_thuc DATETIME,
    ma_tai_khoan CHAR(11),
	thoi_gian_tao datetime default getdate(),
	da_xoa bit default 0,
    FOREIGN KEY (ma_tai_khoan) REFERENCES TaiKhoan(ma_tai_khoan)
);

-- Create NoiThatTrongLoaiPhong table
CREATE TABLE NoiThatTrongLoaiPhong (
    ma_noi_that_trong_loai_phong CHAR(11) PRIMARY KEY,
	so_luong int default 1,
	ma_loai_phong CHAR(11),
	ma_noi_that CHAR(11),
	thoi_gian_tao datetime default getdate(),
	da_xoa bit default 0,
    FOREIGN KEY (ma_loai_phong) REFERENCES LoaiPhong(ma_loai_phong),
	FOREIGN KEY (ma_noi_that) REFERENCES NoiThat(ma_noi_that)
); --Chua co


-- Create GiaPhong table
CREATE TABLE GiaPhong (
    ma_gia_phong CHAR(11) PRIMARY KEY,
    gia_ngay_cu real,
    gia_gio_cu real,
    gia_ngay_moi real not null,
	gia_gio_moi real not null,
	ma_loai_phong CHAR(11),
	ma_phien_dang_nhap CHAR(11),
	thoi_gian_tao datetime default getdate(),
	da_xoa bit default 0,
    FOREIGN KEY (ma_loai_phong) REFERENCES LoaiPhong(ma_loai_phong),
    FOREIGN KEY (ma_phien_dang_nhap) REFERENCES PhienDangNhap(ma_phien_dang_nhap)
);

-- Create KhachHang table
CREATE TABLE KhachHang (
    ma_khach_hang CHAR(11) PRIMARY KEY,
	CCCD NVARCHAR(20) not null,
    ten_khach_hang NVARCHAR(255) not null,
    so_dien_thoai NVARCHAR(20) not null,
	thoi_gian_tao datetime default getdate(),
	da_xoa bit default 0,
);
-- Create DonDatPhong table
CREATE TABLE DonDatPhong (
    ma_don_dat_phong CHAR(11) PRIMARY KEY,
    mo_ta NVARCHAR(255),
    tg_nhan_phong DATETIME not null,
    tg_tra_phong DATETIME not null,
    tong_tien_du_tinh real not null,
    tien_dat_coc real not null,
	da_dat_truoc bit default 0,
	ma_khach_hang CHAR(11),
	ma_phien_dang_nhap CHAR(11),
	thoi_gian_tao datetime default getdate(),
	da_xoa bit default 0,
    FOREIGN KEY (ma_khach_hang) REFERENCES KhachHang(ma_khach_hang),
    FOREIGN KEY (ma_phien_dang_nhap) REFERENCES PhienDangNhap(ma_phien_dang_nhap)
);

-- Create ChiTietDatPhong table
CREATE TABLE ChiTietDatPhong (
    ma_chi_tiet_dat_phong CHAR(11) PRIMARY KEY,
    tg_nhan_phong DATETIME not null,
    tg_tra_phong DATETIME not null,
	kieu_ket_thuc NVARCHAR(255),
    ma_don_dat_phong CHAR(11),
	ma_phong CHAR(11),
	ma_phien_dang_nhap CHAR(11),
	thoi_gian_tao datetime default getdate(),
	da_xoa bit default 0,
    FOREIGN KEY (ma_phien_dang_nhap) REFERENCES PhienDangNhap(ma_phien_dang_nhap),
	FOREIGN KEY (ma_phong) REFERENCES Phong(ma_phong),
    FOREIGN KEY (ma_don_dat_phong) REFERENCES DonDatPhong(ma_don_dat_phong),
);

-- Create LichSuDiVao table
CREATE TABLE LichSuDiVao (
    ma_lich_su_di_vao CHAR(11) PRIMARY KEY,
	la_lan_dau_tien bit default 0,
    ma_chi_tiet_dat_phong CHAR(11),
	thoi_gian_tao datetime default getdate(),
	da_xoa bit default 0,
    FOREIGN KEY (ma_chi_tiet_dat_phong) REFERENCES ChiTietDatPhong(ma_chi_tiet_dat_phong)
);

-- Create LichSuRaNgoai table
CREATE TABLE LichSuRaNgoai (
    ma_lich_su_ra_ngoai CHAR(11) PRIMARY KEY,
	la_lan_cuoi_cung bit default 0,
    ma_chi_tiet_dat_phong CHAR(11),
	thoi_gian_tao datetime default getdate(),
	da_xoa bit default 0,
    FOREIGN KEY (ma_chi_tiet_dat_phong) REFERENCES ChiTietDatPhong(ma_chi_tiet_dat_phong)
);


-- Create LoaiDichVu table
CREATE TABLE LoaiDichVu (
    ma_loai_dich_vu CHAR(11) PRIMARY KEY,
    ten_dich_vu NVARCHAR(255) not null,
	thoi_gian_tao datetime default getdate(),
	da_xoa bit default 0,
); --Chưa có

-- Create DichVu table
CREATE TABLE DichVu (
    ma_dich_vu CHAR(11) PRIMARY KEY,
    ten_dich_vu NVARCHAR(255) not null,
    ma_loai_dich_vu CHAR(11),
	thoi_gian_tao datetime default getdate(),
	da_xoa bit default 0,
    FOREIGN KEY (ma_loai_dich_vu) REFERENCES LoaiDichVu(ma_loai_dich_vu),
);--Chưa có

-- Create DichVu table
CREATE TABLE GiaDichVu (
    ma_gia_dich_vu CHAR(11) PRIMARY KEY,
	gia_cu real,
	gia_moi real not null,
    thoi_gian_tao datetime default getdate(),
	ma_phien_dang_nhap CHAR(11),
    ma_dich_vu CHAR(11),
	da_xoa bit default 0,
    FOREIGN KEY (ma_phien_dang_nhap) REFERENCES PhienDangNhap(ma_phien_dang_nhap),
    FOREIGN KEY (ma_dich_vu) REFERENCES DichVu(ma_dich_vu),
); -- chưa có
-- Create PhongUsageInven table
CREATE TABLE PhongDungDichVu (
    ma_phong_dung_dich_vu CHAR(11) PRIMARY KEY,
    tong_tien real not null,
    so_luong int default 1,
	thoi_gian_dung DATETIME default getdate(),
    gia_thoi_diem_do real not null,
    ma_chi_tiet_dat_phong CHAR(11),
    ma_dich_vu CHAR(11),
    ma_phien_dang_nhap CHAR(11),
	thoi_gian_tao datetime default getdate(),
	da_xoa bit default 0,
    FOREIGN KEY (ma_chi_tiet_dat_phong) REFERENCES ChiTietDatPhong(ma_chi_tiet_dat_phong),
    FOREIGN KEY (ma_dich_vu) REFERENCES DichVu(ma_dich_vu),
    FOREIGN KEY (ma_phien_dang_nhap) REFERENCES PhienDangNhap(ma_phien_dang_nhap),
);-- chưa có


-- Create Invoice table
CREATE TABLE HoaDon (
    ma_hoa_don CHAR(11) PRIMARY KEY,
    phuong_thuc_thanh_toan NVARCHAR(255),
	tong_tien real not null,
	tien_thue real not null,
    tong_hoa_don real not null,
	kieu_hoa_don NVARCHAR(255) not null,
	tinh_trang_thanh_toan NVARCHAR(255) default N'Chưa thanh toán',
    ma_phien_dang_nhap CHAR(11),
    ma_don_dat_phong CHAR(11),
    ma_khach_hang CHAR(11),
	thoi_gian_tao datetime default getdate(),
	da_xoa bit default 0,
    FOREIGN KEY (ma_phien_dang_nhap) REFERENCES PhienDangNhap(ma_phien_dang_nhap),
    FOREIGN KEY (ma_don_dat_phong) REFERENCES DonDatPhong(ma_don_dat_phong),
    FOREIGN KEY (ma_khach_hang) REFERENCES KhachHang(ma_khach_hang)
);

--create ChiTietHoaDon
CREATE TABLE ChiTietHoaDon (
    ma_chi_tiet_hoa_don CHAR(11) PRIMARY KEY,
	thoi_gian_su_dung int,
	tong_chi_tiet real,
	ma_hoa_don CHAR(11),
	ma_chi_tiet_dat_phong CHAR(11),
	thoi_gian_tao datetime default getdate(),
	da_xoa bit default 0,
	gia_gio_thoi_diem_do real not null,
	gia_ngay_thoi_diem_do real not null,
	FOREIGN KEY (ma_chi_tiet_dat_phong) REFERENCES ChiTietDatPhong(ma_chi_tiet_dat_phong),
    FOREIGN KEY (ma_hoa_don) REFERENCES HoaDon(ma_hoa_don),
);

-- Create PhuPhi table
CREATE TABLE PhuPhi (
    ma_phu_phi CHAR(11) PRIMARY KEY,
    ma_phu_phi CHAR(11) PRIMARY KEY,
    ten_phu_phi NVARCHAR(255) not null,
	thoi_gian_tao datetime default getdate(),
	da_xoa bit default 0,
);

-- Create GiaPhuPhi table
CREATE TABLE GiaPhuPhi (
    ma_gia_phu_phi CHAR(11) PRIMARY KEY,
    gia_truoc_do real,
	gia_hien_tai real not null,
	la_phan_tram bit default 0,
    ma_phien_dang_nhap CHAR(11),
    ma_phu_phi CHAR(11),
	thoi_gian_tao datetime default getdate(),
	da_xoa bit default 0,
    FOREIGN KEY (ma_phien_dang_nhap) REFERENCES PhienDangNhap(ma_phien_dang_nhap),
    FOREIGN KEY (ma_phu_phi) REFERENCES PhuPhi(ma_phu_phi)
);

--create table ChiTietDatPhong_Fee
CREATE TABLE PhongTinhPhuPhi(
    ma_phong_tinh_phu_phi CHAR(11) PRIMARY KEY,
	time_stamp datetime default getdate(),
	ma_chi_tiet_dat_phong CHAR(11),
	ma_phu_phi CHAR(11),
	thoi_gian_tao datetime default getdate(),
	da_xoa bit default 0,
	gia_thoi_diem_do real not null,
	FOREIGN KEY (ma_chi_tiet_dat_phong) REFERENCES ChiTietDatPhong(ma_chi_tiet_dat_phong),
	FOREIGN KEY (ma_phu_phi) REFERENCES PhuPhi(ma_phu_phi)
)


--create table BienBan
CREATE TABLE BienBan(
	ma_bien_ban CHAR(11) PRIMARY KEY,
	li_do NVARCHAR(255) not null,
	phi_bien_ban real,
	ma_chi_tiet_dat_phong CHAR(11),
	ma_phien_dang_nhap CHAR(11),
	thoi_gian_tao datetime default getdate(),
	da_xoa bit default 0,
	FOREIGN KEY (ma_chi_tiet_dat_phong) REFERENCES ChiTietDatPhong(ma_chi_tiet_dat_phong),
	FOREIGN KEY (ma_phien_dang_nhap) REFERENCES PhienDangNhap(ma_phien_dang_nhap),
)

-- Create LichSuThaoTac table
CREATE TABLE LichSuThaoTac (
    ma_lich_su_thao_tac CHAR(11) PRIMARY KEY,
    ten_thao_tac NVARCHAR(255) not null,
    mo_ta NVARCHAR(255),
    ma_phien_dang_nhap CHAR(11),
	thoi_gian_tao datetime default getdate(),
	da_xoa bit default 0,
    FOREIGN KEY (ma_phien_dang_nhap) REFERENCES PhienDangNhap(ma_phien_dang_nhap),
);

-- Create ThongBao table
CREATE TABLE ThongBao (
    ma_thong_bao CHAR(11) PRIMARY KEY,
    noi_dung NVARCHAR(255) not null,
    ma_phien_dang_nhap CHAR(11),
	thoi_gian_tao datetime default getdate(),
	da_xoa bit default 0,
    FOREIGN KEY (ma_phien_dang_nhap) REFERENCES PhienDangNhap(ma_phien_dang_nhap)
);

--Data mẫu
--Loại phòng
INSERT INTO LoaiPhong (ma_loai_phong, ten_loai_phong, so_luong_khach, phan_loai)
VALUES
('RC00000002', N'Phòng thường 1 giường đôi', 2, N'Thường'),
('RC00000001', N'Phòng thường 1 giường đơn', 1, N'Thường'),
('RC00000003', N'Phòng thường 2 giường đôi', 4, N'Thường'),
('RC00000004', N'Phòng vip 1 giường đơn', 1, N'Vip'),
('RC00000005', N'Phòng vip 1 giường đôi', 2, N'Vip'),
('RC00000006', N'Phòng vip 2 giường đôi', 4, N'Vip');


--Phòng
-- RC00000001 - Phòng 1 giường đơn (Thường)
INSERT INTO Phong (ma_phong, ten_phong, ma_loai_phong)
VALUES
('RO00000001', N'Phòng T001', 'RC00000001'),
('RO00000002', N'Phòng T002', 'RC00000001'),
('RO00000003', N'Phòng T003', 'RC00000001'),
('RO00000004', N'Phòng T004', 'RC00000001'),
('RO00000005', N'Phòng T005', 'RC00000001'),
('RO00000006', N'Phòng T006', 'RC00000001'),
('RO00000007', N'Phòng T007', 'RC00000001'),
('RO00000008', N'Phòng T008', 'RC00000001'),
('RO00000009', N'Phòng T009', 'RC00000001'),
('RO00000010', N'Phòng T010', 'RC00000001');

-- RC00000002 - Phòng 1 giường đôi (Thường)
INSERT INTO Phong (ma_phong, ten_phong, ma_loai_phong)
VALUES
('RO00000011', N'Phòng T011', 'RC00000002'),
('RO00000012', N'Phòng T012', 'RC00000002'),
('RO00000013', N'Phòng T013', 'RC00000002'),
('RO00000014', N'Phòng T014', 'RC00000002'),
('RO00000015', N'Phòng T015', 'RC00000002'),
('RO00000016', N'Phòng T016', 'RC00000002'),
('RO00000017', N'Phòng T017', 'RC00000002'),
('RO00000018', N'Phòng T018', 'RC00000002'),
('RO00000019', N'Phòng T019', 'RC00000002'),
('RO00000020', N'Phòng T020', 'RC00000002');

-- RC00000003 - Phòng 2 giường đôi (Thường)
INSERT INTO Phong (ma_phong, ten_phong, ma_loai_phong)
VALUES
('RO00000021', N'Phòng T021', 'RC00000003'),
('RO00000022', N'Phòng T022', 'RC00000003'),
('RO00000023', N'Phòng T023', 'RC00000003'),
('RO00000024', N'Phòng T024', 'RC00000003'),
('RO00000025', N'Phòng T025', 'RC00000003'),
('RO00000026', N'Phòng T026', 'RC00000003'),
('RO00000027', N'Phòng T027', 'RC00000003'),
('RO00000028', N'Phòng T028', 'RC00000003'),
('RO00000029', N'Phòng T029', 'RC00000003'),
('RO00000030', N'Phòng T030', 'RC00000003');

-- RC00000004 - Phòng 1 giường đơn (Vip)
INSERT INTO Phong (ma_phong, ten_phong, ma_loai_phong)
VALUES
('RO00000031', N'Phòng V031', 'RC00000004'),
('RO00000032', N'Phòng V032', 'RC00000004'),
('RO00000033', N'Phòng V033', 'RC00000004'),
('RO00000034', N'Phòng V034', 'RC00000004'),
('RO00000035', N'Phòng V035', 'RC00000004'),
('RO00000036', N'Phòng V036', 'RC00000004'),
('RO00000037', N'Phòng V037', 'RC00000004'),
('RO00000038', N'Phòng V038', 'RC00000004'),
('RO00000039', N'Phòng V039', 'RC00000004'),
('RO00000040', N'Phòng V040', 'RC00000004');

-- RC00000005 - Phòng 1 giường đôi (Vip)
INSERT INTO Phong (ma_phong, ten_phong, ma_loai_phong)
VALUES
('RO00000041', N'Phòng V041', 'RC00000005'),
('RO00000042', N'Phòng V042', 'RC00000005'),
('RO00000043', N'Phòng V043', 'RC00000005'),
('RO00000044', N'Phòng V044', 'RC00000005'),
('RO00000045', N'Phòng V045', 'RC00000005'),
('RO00000046', N'Phòng V046', 'RC00000005'),
('RO00000047', N'Phòng V047', 'RC00000005'),
('RO00000048', N'Phòng V048', 'RC00000005'),
('RO00000049', N'Phòng V049', 'RC00000005'),
('RO00000050', N'Phòng V050', 'RC00000005');

-- RC00000006 - Phòng 2 giường đôi (Vip)
INSERT INTO Phong (ma_phong, ten_phong, ma_loai_phong)
VALUES
('RO00000051', N'Phòng V051', 'RC00000006'),
('RO00000052', N'Phòng V052', 'RC00000006'),
('RO00000053', N'Phòng V053', 'RC00000006'),
('RO00000054', N'Phòng V054', 'RC00000006'),
('RO00000055', N'Phòng V055', 'RC00000006'),
('RO00000056', N'Phòng V056', 'RC00000006'),
('RO00000057', N'Phòng V057', 'RC00000006'),
('RO00000058', N'Phòng V058', 'RC00000006'),
('RO00000059', N'Phòng V059', 'RC00000006'),
('RO00000060', N'Phòng V060', 'RC00000006');





--Role
INSERT INTO ChucVu (ma_chuc_vu, ten_chuc_vu)
VALUES
('UR001', N'Nhân viên'),
('UR002', N'Quản lý');

--Employee
INSERT INTO NhanVien (ma_nhan_vien, ten_nhan_vien, CCCD, ngay_sinh, so_dien_thoai)
VALUES
('EM00000001', N'Nguyễn Văn An',   N'012345678901', '1995-03-15', '0901234567'),
('EM00000002', N'Trần Thị Bình',   N'012345678902', '1997-07-20', '0912345678'),
('EM00000003', N'Lê Văn Cường',   N'012345678903', '1992-11-05', '0923456789'),
('EM00000004', N'Phạm Thị Dung',  N'012345678904', '1998-01-12', '0934567890'),
('EM00000005', N'Hoàng Văn Em',   N'012345678905', '1994-09-25', '0945678901'),
('EM00000006', N'Đỗ Thị Hoa',     N'012345678906', '1996-06-18', '0956789012'),
('EM00000007', N'Ngô Văn Hùng',   N'012345678907', '1993-04-02', '0967890123'),
('EM00000008', N'Vũ Thị Lan',     N'012345678908', '1999-12-22', '0978901234'),
('EM00000009', N'Bùi Văn Minh',   N'012345678909', '1991-08-30', '0989012345'),
('EM00000010', N'Phan Thị Ngọc',  N'012345678910', '1995-05-10', '0990123456');

--TaiKhoan
-- 8 Nhân viên
INSERT INTO TaiKhoan (ma_tai_khoan, ten_dang_nhap, mat_khau, ma_chuc_vu, ma_nhan_vien)
VALUES
('AC00000001', N'an.nguyen',  '123456', 'UR001', 'EM00000001'),
('AC00000002', N'binh.tran',  '123456', 'UR001', 'EM00000002'),
('AC00000003', N'cuong.le',   '123456', 'UR001', 'EM00000003'),
('AC00000004', N'dung.pham',  '123456', 'UR001', 'EM00000004'),
('AC00000005', N'em.hoang',   '123456', 'UR001', 'EM00000005'),
('AC00000006', N'hoa.do',     '123456', 'UR001', 'EM00000006'),
('AC00000007', N'hung.ngo',   '123456', 'UR001', 'EM00000007'),
('AC00000008', N'lan.vu',     '123456', 'UR001', 'EM00000008');

-- 2 Quản lý
INSERT INTO TaiKhoan (ma_tai_khoan, ten_dang_nhap, mat_khau, ma_chuc_vu, ma_nhan_vien)
VALUES
('AC00000009', N'minh.bui',   '123456', 'UR002', 'EM00000009'),
('AC00000010', N'ngoc.phan',  '123456', 'UR002', 'EM00000010');

--ShiftAssigment
INSERT INTO PhienDangNhap (ma_phien_dang_nhap, tg_bat_dau, tg_ket_thuc, ma_tai_khoan)
VALUES
-- EM00000001 (AC00000001)
('SA00000001', '2025-09-01 08:00:00', '2025-09-01 12:00:00', 'AC00000001'),
('SA00000002', '2025-09-03 13:00:00', '2025-09-03 17:00:00', 'AC00000001'),

-- EM00000002 (AC00000002)
('SA00000003', '2025-09-02 08:00:00', '2025-09-02 12:00:00', 'AC00000002'),
('SA00000004', '2025-09-04 13:00:00', '2025-09-04 17:00:00', 'AC00000002'),

-- EM00000003 (AC00000003)
('SA00000005', '2025-09-05 08:00:00', '2025-09-05 12:00:00', 'AC00000003'),
('SA00000006', '2025-09-07 13:00:00', '2025-09-07 17:00:00', 'AC00000003'),

-- EM00000004 (AC00000004)
('SA00000007', '2025-09-06 08:00:00', '2025-09-06 12:00:00', 'AC00000004'),
('SA00000008', '2025-09-08 13:00:00', '2025-09-08 17:00:00', 'AC00000004'),

-- EM00000005 (AC00000005)
('SA00000009', '2025-09-09 08:00:00', '2025-09-09 12:00:00', 'AC00000005'),
('SA00000010', '2025-09-11 13:00:00', '2025-09-11 17:00:00', 'AC00000005'),

-- EM00000006 (AC00000006)
('SA00000011', '2025-09-10 08:00:00', '2025-09-10 12:00:00', 'AC00000006'),
('SA00000012', '2025-09-12 13:00:00', '2025-09-12 17:00:00', 'AC00000006'),

-- EM00000007 (AC00000007)
('SA00000013', '2025-09-13 08:00:00', '2025-09-13 12:00:00', 'AC00000007'),
('SA00000014', '2025-09-15 13:00:00', '2025-09-15 17:00:00', 'AC00000007'),

-- EM00000008 (AC00000008)
('SA00000015', '2025-09-14 08:00:00', '2025-09-14 12:00:00', 'AC00000008'),
('SA00000016', '2025-09-16 13:00:00', '2025-09-16 17:00:00', 'AC00000008'),

-- EM00000009 (AC00000009 - quản lý)
('SA00000017', '2025-09-17 08:00:00', '2025-09-17 12:00:00', 'AC00000009'),
('SA00000018', '2025-09-19 13:00:00', '2025-09-19 17:00:00', 'AC00000009'),

-- EM00000010 (AC00000010 - quản lý)
('SA00000019', '2025-09-18 08:00:00', '2025-09-18 12:00:00', 'AC00000010'),
('SA00000020', '2025-09-20 13:00:00', '2025-09-20 17:00:00', 'AC00000010');


--GiaPhong
INSERT INTO GiaPhong (ma_gia_phong, gia_ngay_moi, gia_gio_moi, ma_loai_phong, ma_phien_dang_nhap)
VALUES
-- RC00000001: Phòng 1 giường đơn (Thường)
('RP00000001', 300000, 50000, 'RC00000001', 'SA00000001'),

-- RC00000002: Phòng 1 giường đôi (Thường)
('RP00000002', 45000, 70000, 'RC00000002', 'SA00000003'),

-- RC00000003: Phòng 2 giường đôi (Thường)
('RP00000003', 600000, 100000, 'RC00000003', 'SA00000006'),

-- RC00000004: Phòng 1 giường đơn (Vip)
('RP00000004', 500000, 80000, 'RC00000004', 'SA00000008'),

-- RC00000005: Phòng 1 giường đôi (Vip)
('RP00000005', 700000, 120000, 'RC00000005', 'SA00000010'),

-- RC00000006: Phòng 2 giường đôi (Vip)
('RP00000006', 900000, 150000, 'RC00000006', 'SA00000011');


--KhachHang
INSERT INTO KhachHang (ma_khach_hang, CCCD, ten_khach_hang, so_dien_thoai)
VALUES
('CS00000001', N'079123456789', N'Nguyễn Văn A', '0912345678'),
('CS00000002', N'079987654321', N'Trần Thị B', '0987654321'),
('CS00000003', N'079456789123', N'Lê Văn C', '0905123456'),
('CS00000004', N'079321654987', N'Phạm Thị D', '0934567890'),
('CS00000005', N'079741258963', N'Huỳnh Văn E', '0978123456');


--RF
INSERT INTO DonDatPhong 
(ma_don_dat_phong, mo_ta, tg_nhan_phong, tg_tra_phong, tong_tien_du_tinh, tien_dat_coc, da_dat_truoc, ma_khach_hang, ma_phien_dang_nhap)
VALUES
('RF00000001', N'Đặt trước qua điện thoại', '2025-09-05', '2025-09-07', 1200000, 500000, 1, 'CS00000001', 'SA00000001'),
('RF00000002', N'Khách quen', '2025-09-06', '2025-09-08', 2000000, 800000, 1, 'CS00000002', 'SA00000002'),
('RF00000003', N'Yêu cầu tầng 2', '2025-09-09', '2025-09-11', 1500000, 600000, 0, 'CS00000003', 'SA00000003'),
('RF00000004', N'Thanh toán khi nhận phòng', '2025-09-10', '2025-09-12', 1800000, 900000, 1, 'CS00000004', 'SA00000004'),
('RF00000005', N'Check-in trễ 22h', '2025-09-12', '2025-09-14', 2200000, 1000000, 0, 'CS00000005', 'SA00000005'),
('RF00000006', N'Đặt online', '2025-09-14', '2025-09-16', 2500000, 1200000, 1, 'CS00000002', 'SA00000006'),
('RF00000007', N'Cần thêm giường phụ', '2025-09-17', '2025-09-19', 2800000, 1400000, 1, 'CS00000001', 'SA00000007'),
('RF00000008', N'Khách công tác', '2025-09-18', '2025-09-20', 3000000, 1500000, 0, 'CS00000005', 'SA00000008'),
('RF00000009', N'Khách đoàn 4 người', '2025-09-20', '2025-09-23', 3500000, 1700000, 1, 'CS00000004', 'SA00000009'),
('RF00000010', N'Khách yêu cầu phòng VIP', '2025-09-22', '2025-09-25', 4000000, 2000000, 1, 'CS00000003', 'SA00000010');

--RD
INSERT INTO ChiTietDatPhong (ma_chi_tiet_dat_phong, tg_nhan_phong, tg_tra_phong, ma_don_dat_phong, ma_phong, ma_phien_dang_nhap)
VALUES
('RD00000001', '2025-09-07', '2025-09-05', 'RF00000001', 'RO00000001', 'SA00000001'),
('RD00000002', '2025-09-08', '2025-09-06', 'RF00000002', 'RO00000011', 'SA00000002'),
('RD00000003', '2025-09-11', '2025-09-09', 'RF00000003', 'RO00000021', 'SA00000003'),
('RD00000004', '2025-09-12', '2025-09-10', 'RF00000004', 'RO00000031', 'SA00000004'),
('RD00000005', '2025-09-14', '2025-09-12', 'RF00000005', 'RO00000041', 'SA00000005'),
('RD00000006', '2025-09-16', '2025-09-14', 'RF00000006', 'RO00000051', 'SA00000006'),
('RD00000007', '2025-09-19', '2025-09-17', 'RF00000007', 'RO00000002', 'SA00000007'),
('RD00000008', '2025-09-20', '2025-09-18', 'RF00000008', 'RO00000012', 'SA00000008'),
('RD00000009', '2025-09-23', '2025-09-20', 'RF00000009', 'RO00000022', 'SA00000009'),
('RD00000010', '2025-09-25', '2025-09-22', 'RF00000010', 'RO00000032', 'SA00000010');


--Check in check out
INSERT INTO LichSuDiVao (ma_lich_su_di_vao, la_lan_dau_tien, ma_chi_tiet_dat_phong)
VALUES
('HI00000001',  1, 'RD00000001'),
('HI00000002',  1, 'RD00000002'),
('HI00000003',  1, 'RD00000003'),
('HI00000004',  1, 'RD00000004'),
('HI00000005',  1, 'RD00000005'),
('HI00000006',  1, 'RD00000006'),
('HI00000007',  1, 'RD00000007'),
('HI00000008',  1, 'RD00000008'),
('HI00000009',  1, 'RD00000009'),
('HI00000010',  1, 'RD00000010');

-- Lịch sử CheckOut
INSERT INTO LichSuRaNgoai (ma_lich_su_ra_ngoai, la_lan_cuoi_cung, ma_chi_tiet_dat_phong)
VALUES
('HO00000001',  1, 'RD00000001'),
('HO00000002',  1, 'RD00000002'),
('HO00000003',  1, 'RD00000003'),
('HO00000004',  1, 'RD00000004'),
('HO00000005',  1, 'RD00000005'),
('HO00000006',  1, 'RD00000006'),
('HO00000007',  1, 'RD00000007'),
('HO00000008',  1, 'RD00000008'),
('HO00000009',  1, 'RD00000009'),
('HO00000010',  1, 'RD00000010');

--Invoicec
INSERT INTO HoaDon (ma_hoa_don, phuong_thuc_thanh_toan, tong_tien, tien_thue, tong_hoa_don, kieu_hoa_don, tinh_trang_thanh_toan, ma_phien_dang_nhap, ma_don_dat_phong, ma_khach_hang)
VALUES
('IN00000001', N'Tiền mặt',     1200000, 120000, 1320000, N'Đặt phòng', N'Đã thanh toán',     'SA00000001', 'RF00000001', 'CS00000001'),
('IN00000002', N'Thẻ tín dụng', 2000000, 200000, 2200000, N'Đặt phòng', N'Đã thanh toán',     'SA00000002', 'RF00000002', 'CS00000002'),
('IN00000003', N'Chuyển khoản', 1500000, 150000, 1650000, N'Đặt phòng', N'Đã thanh toán',  'SA00000003', 'RF00000003', 'CS00000003'),
('IN00000004', N'Tiền mặt',     1800000, 180000, 1980000, N'Đặt phòng', N'Đã thanh toán',     'SA00000004', 'RF00000004', 'CS00000004'),
('IN00000005', N'Tiền mặt',     2200000, 220000, 2420000, N'Đặt phòng', N'Đã thanh toán',  'SA00000005', 'RF00000005', 'CS00000005'),
('IN00000006', N'Thẻ tín dụng', 2500000, 250000, 2750000, N'Đặt phòng', N'Đã thanh toán',     'SA00000006', 'RF00000006', 'CS00000002'),
('IN00000007', N'Chuyển khoản', 2800000, 280000, 3080000, N'Đặt phòng', N'Đã thanh toán',     'SA00000007', 'RF00000007', 'CS00000001'),
('IN00000008', N'Tiền mặt',     3000000, 300000, 3300000, N'Đặt phòng', N'Đã thanh toán',  'SA00000008', 'RF00000008', 'CS00000005'),
('IN00000009', N'Tiền mặt',     3500000, 350000, 3850000, N'Đặt phòng', N'Đã thanh toán',     'SA00000009', 'RF00000009', 'CS00000004'),
('IN00000010', N'Thẻ tín dụng', 4000000, 400000, 4400000, N'Đặt phòng', N'Đã thanh toán',     'SA00000010', 'RF00000010', 'CS00000003');


USE master;
GO

IF EXISTS (SELECT name FROM sys.databases WHERE name = N'QLKS')
BEGIN
	ALTER DATABASE [QLKS] SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE QLKS;
END
GO
CREATE DATABASE QLKS;
GO
use QLKS
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
    loai NVARCHAR(255) default N'ĐẶT ĐƠN',
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
    ten_loai_dich_vu NVARCHAR(255) not null,
	thoi_gian_tao datetime default getdate(),
	da_xoa bit default 0,
); --Chưa có

-- Create DichVu table
CREATE TABLE DichVu (
    ma_dich_vu CHAR(11) PRIMARY KEY,
    ten_dich_vu NVARCHAR(255) not null,
    ton_kho int default 0,
    co_the_tang bit default 0,
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
    so_luong int default 1,
	thoi_gian_dung DATETIME default getdate(),
    gia_thoi_diem_do real not null,
    ma_chi_tiet_dat_phong CHAR(11),
    ma_dich_vu CHAR(11),
    ma_phien_dang_nhap CHAR(11),
	thoi_gian_tao datetime default getdate(),
	da_xoa bit default 0,
	tong_tien real,
    FOREIGN KEY (ma_chi_tiet_dat_phong) REFERENCES ChiTietDatPhong(ma_chi_tiet_dat_phong),
    FOREIGN KEY (ma_dich_vu) REFERENCES DichVu(ma_dich_vu),
    FOREIGN KEY (ma_phien_dang_nhap) REFERENCES PhienDangNhap(ma_phien_dang_nhap),
);-- chưa có


-- Create Invoice table
CREATE TABLE HoaDon (
    ma_hoa_don CHAR(11) PRIMARY KEY,
    phuong_thuc_thanh_toan NVARCHAR(255) default N'TIỀN MẶT',
	kieu_hoa_don NVARCHAR(255) not null default N'HOÁ ĐƠN THANH TOÁN',
	tinh_trang_thanh_toan NVARCHAR(255) default N'CHƯA THANH TOÁN',
    ma_phien_dang_nhap CHAR(11),
    ma_don_dat_phong CHAR(11),
    ma_khach_hang CHAR(11),
	thoi_gian_tao datetime default getdate(),
	tong_tien real,
	tien_thue real,
	tong_hoa_don real,
    FOREIGN KEY (ma_phien_dang_nhap) REFERENCES PhienDangNhap(ma_phien_dang_nhap),
    FOREIGN KEY (ma_don_dat_phong) REFERENCES DonDatPhong(ma_don_dat_phong),
    FOREIGN KEY (ma_khach_hang) REFERENCES KhachHang(ma_khach_hang)
);

--create ChiTietHoaDon
CREATE TABLE ChiTietHoaDon (
    ma_chi_tiet_hoa_don CHAR(11) PRIMARY KEY,
	thoi_gian_su_dung int not null default 0,
	ma_hoa_don CHAR(11),
	ma_chi_tiet_dat_phong CHAR(11),
    ma_phong CHAR(11),
	thoi_gian_tao datetime default getdate(),
	da_xoa bit default 0,
	don_gia_phong real not null default 0,
	tong_tien real,
	FOREIGN KEY (ma_chi_tiet_dat_phong) REFERENCES ChiTietDatPhong(ma_chi_tiet_dat_phong),
    FOREIGN KEY (ma_hoa_don) REFERENCES HoaDon(ma_hoa_don),
    FOREIGN KEY (ma_phong) REFERENCES Phong(ma_phong)
);

-- Create PhuPhi table
CREATE TABLE PhuPhi (
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
	ma_chi_tiet_dat_phong CHAR(11),
	ma_phu_phi CHAR(11),
	thoi_gian_tao datetime default getdate(),
	da_xoa bit default 0,
	don_gia_phu_phi real, 
	tong_tien real,
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
('LP00000001', N'Phòng thường 1 giường đơn', 1, N'Thường'),
('LP00000002', N'Phòng thường 1 giường đôi', 2, N'Thường'),
('LP00000003', N'Phòng thường 2 giường đôi', 4, N'Thường'),
('LP00000004', N'Phòng vip 1 giường đơn', 1, N'Vip'),
('LP00000005', N'Phòng vip 1 giường đôi', 2, N'Vip'),
('LP00000006', N'Phòng vip 2 giường đôi', 4, N'Vip');


--Phòng
-- LP00000001 - Phòng 1 giường đơn (Thường)
INSERT INTO Phong (ma_phong, ten_phong, ma_loai_phong)
VALUES
('PH00000001', N'Phòng T001', 'LP00000001'),
('PH00000002', N'Phòng T002', 'LP00000001'),
('PH00000003', N'Phòng T003', 'LP00000001'),
('PH00000004', N'Phòng T004', 'LP00000001'),
('PH00000005', N'Phòng T005', 'LP00000001'),
('PH00000006', N'Phòng T006', 'LP00000001'),
('PH00000007', N'Phòng T007', 'LP00000001'),
('PH00000008', N'Phòng T008', 'LP00000001'),
('PH00000009', N'Phòng T009', 'LP00000001'),
('PH00000010', N'Phòng T010', 'LP00000001');

-- LP00000002 - Phòng 1 giường đôi (Thường)
INSERT INTO Phong (ma_phong, ten_phong, ma_loai_phong)
VALUES
('PH00000011', N'Phòng T011', 'LP00000002'),
('PH00000012', N'Phòng T012', 'LP00000002'),
('PH00000013', N'Phòng T013', 'LP00000002'),
('PH00000014', N'Phòng T014', 'LP00000002'),
('PH00000015', N'Phòng T015', 'LP00000002'),
('PH00000016', N'Phòng T016', 'LP00000002'),
('PH00000017', N'Phòng T017', 'LP00000002'),
('PH00000018', N'Phòng T018', 'LP00000002'),
('PH00000019', N'Phòng T019', 'LP00000002'),
('PH00000020', N'Phòng T020', 'LP00000002');

-- LP00000003 - Phòng 2 giường đôi (Thường)
INSERT INTO Phong (ma_phong, ten_phong, ma_loai_phong)
VALUES
('PH00000021', N'Phòng T021', 'LP00000003'),
('PH00000022', N'Phòng T022', 'LP00000003'),
('PH00000023', N'Phòng T023', 'LP00000003'),
('PH00000024', N'Phòng T024', 'LP00000003'),
('PH00000025', N'Phòng T025', 'LP00000003'),
('PH00000026', N'Phòng T026', 'LP00000003'),
('PH00000027', N'Phòng T027', 'LP00000003'),
('PH00000028', N'Phòng T028', 'LP00000003'),
('PH00000029', N'Phòng T029', 'LP00000003'),
('PH00000030', N'Phòng T030', 'LP00000003');

-- LP00000004 - Phòng 1 giường đơn (Vip)
INSERT INTO Phong (ma_phong, ten_phong, ma_loai_phong)
VALUES
('PH00000031', N'Phòng V031', 'LP00000004'),
('PH00000032', N'Phòng V032', 'LP00000004'),
('PH00000033', N'Phòng V033', 'LP00000004'),
('PH00000034', N'Phòng V034', 'LP00000004'),
('PH00000035', N'Phòng V035', 'LP00000004'),
('PH00000036', N'Phòng V036', 'LP00000004'),
('PH00000037', N'Phòng V037', 'LP00000004'),
('PH00000038', N'Phòng V038', 'LP00000004'),
('PH00000039', N'Phòng V039', 'LP00000004'),
('PH00000040', N'Phòng V040', 'LP00000004');

-- LP00000005 - Phòng 1 giường đôi (Vip)
INSERT INTO Phong (ma_phong, ten_phong, ma_loai_phong)
VALUES
('PH00000041', N'Phòng V041', 'LP00000005'),
('PH00000042', N'Phòng V042', 'LP00000005'),
('PH00000043', N'Phòng V043', 'LP00000005'),
('PH00000044', N'Phòng V044', 'LP00000005'),
('PH00000045', N'Phòng V045', 'LP00000005'),
('PH00000046', N'Phòng V046', 'LP00000005'),
('PH00000047', N'Phòng V047', 'LP00000005'),
('PH00000048', N'Phòng V048', 'LP00000005'),
('PH00000049', N'Phòng V049', 'LP00000005'),
('PH00000050', N'Phòng V050', 'LP00000005');

-- LP00000006 - Phòng 2 giường đôi (Vip)
INSERT INTO Phong (ma_phong, ten_phong, ma_loai_phong)
VALUES
('PH00000051', N'Phòng V051', 'LP00000006'),
('PH00000052', N'Phòng V052', 'LP00000006'),
('PH00000053', N'Phòng V053', 'LP00000006'),
('PH00000054', N'Phòng V054', 'LP00000006'),
('PH00000055', N'Phòng V055', 'LP00000006'),
('PH00000056', N'Phòng V056', 'LP00000006'),
('PH00000057', N'Phòng V057', 'LP00000006'),
('PH00000058', N'Phòng V058', 'LP00000006'),
('PH00000059', N'Phòng V059', 'LP00000006'),
('PH00000060', N'Phòng V060', 'LP00000006');

-- MAINTAINING Room
INSERT INTO PHONG (ma_phong, ten_phong, dang_hoat_dong, ma_loai_phong)
VALUES
--     ('PH00000061', N'Phòng X061', 0, 'LP00000001'),
--     ('PH00000062', N'Phòng X062', 0, 'LP00000002'),
('PH00000063', N'Phòng X063', 0, 'LP00000001'),
('PH00000064', N'Phòng X064', 0, 'LP00000002');
;


--Role
INSERT INTO ChucVu (ma_chuc_vu, ten_chuc_vu)
VALUES
('CV001', N'Nhân viên'),
('CV002', N'Quản lý');

--Employee
INSERT INTO NhanVien (ma_nhan_vien, ten_nhan_vien, CCCD, ngay_sinh, so_dien_thoai)
VALUES
('NV00000001', N'Nguyễn Văn An',   N'012345678901', '1995-03-15', '0901234567'),
('NV00000002', N'Trần Thị Bình',   N'012345678902', '1997-07-20', '0912345678'),
('NV00000003', N'Lê Văn Cường',   N'012345678903', '1992-11-05', '0923456789'),
('NV00000004', N'Phạm Thị Dung',  N'012345678904', '1998-01-12', '0934567890'),
('NV00000005', N'Hoàng Văn Em',   N'012345678905', '1994-09-25', '0945678901'),
('NV00000006', N'Đỗ Thị Hoa',     N'012345678906', '1996-06-18', '0956789012'),
('NV00000007', N'Ngô Văn Hùng',   N'012345678907', '1993-04-02', '0967890123'),
('NV00000008', N'Vũ Thị Lan',     N'012345678908', '1999-12-22', '0978901234'),
('NV00000009', N'Bùi Văn Minh',   N'012345678909', '1991-08-30', '0989012345'),
('NV00000010', N'Phan Thị Ngọc',  N'012345678910', '1995-05-10', '0990123456');

--TaiKhoan
-- 8 Nhân viên
INSERT INTO TaiKhoan (ma_tai_khoan, ten_dang_nhap, mat_khau, ma_chuc_vu, ma_nhan_vien)
VALUES
('TK00000001', N'an.nguyen',  '$2a$12$L2O6nosraJCbsRQhONtqb.DDdyUlT5KmmM23xpjoq/nwvipURx/i.', 'CV001', 'NV00000001'),
('TK00000002', N'binh.tran',  '$2a$12$L2O6nosraJCbsRQhONtqb.DDdyUlT5KmmM23xpjoq/nwvipURx/i.', 'CV001', 'NV00000002'),
('TK00000003', N'cuong.le',   '$2a$12$L2O6nosraJCbsRQhONtqb.DDdyUlT5KmmM23xpjoq/nwvipURx/i.', 'CV001', 'NV00000003'),
('TK00000004', N'dung.pham',  '$2a$12$L2O6nosraJCbsRQhONtqb.DDdyUlT5KmmM23xpjoq/nwvipURx/i.', 'CV001', 'NV00000004'),
('TK00000005', N'em.hoang',   '$2a$12$L2O6nosraJCbsRQhONtqb.DDdyUlT5KmmM23xpjoq/nwvipURx/i.', 'CV001', 'NV00000005'),
('TK00000006', N'hoa.do',     '$2a$12$L2O6nosraJCbsRQhONtqb.DDdyUlT5KmmM23xpjoq/nwvipURx/i.', 'CV001', 'NV00000006'),
('TK00000007', N'hung.ngo',   '$2a$12$L2O6nosraJCbsRQhONtqb.DDdyUlT5KmmM23xpjoq/nwvipURx/i.', 'CV001', 'NV00000007'),
('TK00000008', N'lan.vu',     '$2a$12$L2O6nosraJCbsRQhONtqb.DDdyUlT5KmmM23xpjoq/nwvipURx/i.', 'CV001', 'NV00000008');

-- 2 Quản lý
INSERT INTO TaiKhoan (ma_tai_khoan, ten_dang_nhap, mat_khau, ma_chuc_vu, ma_nhan_vien)
VALUES
('TK00000009', N'minh.bui',   '$2a$12$HRDcodYwCTvjwsTzwLOK2uBlrNSeNd3EOMcG5fyH2Q9VvTnvrdDiy', 'CV002', 'NV00000009'),
('TK00000010', N'ngoc.phan',  '$2a$12$HRDcodYwCTvjwsTzwLOK2uBlrNSeNd3EOMcG5fyH2Q9VvTnvrdDiy', 'CV002', 'NV00000010');
--ShiftAssigment
INSERT INTO PhienDangNhap (ma_phien_dang_nhap, tg_bat_dau, tg_ket_thuc, ma_tai_khoan)
VALUES
-- NV00000001 (TK00000001)
('PN00000001', '2025-09-01 08:00:00', '2025-09-01 12:00:00', 'TK00000001'),
('PN00000002', '2025-09-03 13:00:00', '2025-09-03 17:00:00', 'TK00000001'),

-- NV00000002 (TK00000002)
('PN00000003', '2025-09-02 08:00:00', '2025-09-02 12:00:00', 'TK00000002'),
('PN00000004', '2025-09-04 13:00:00', '2025-09-04 17:00:00', 'TK00000002'),

-- NV00000003 (TK00000003)
('PN00000005', '2025-09-05 08:00:00', '2025-09-05 12:00:00', 'TK00000003'),
('PN00000006', '2025-09-07 13:00:00', '2025-09-07 17:00:00', 'TK00000003'),

-- NV00000004 (TK00000004)
('PN00000007', '2025-09-06 08:00:00', '2025-09-06 12:00:00', 'TK00000004'),
('PN00000008', '2025-09-08 13:00:00', '2025-09-08 17:00:00', 'TK00000004'),

-- NV00000005 (TK00000005)
('PN00000009', '2025-09-09 08:00:00', '2025-09-09 12:00:00', 'TK00000005'),
('PN00000010', '2025-09-11 13:00:00', '2025-09-11 17:00:00', 'TK00000005'),

-- NV00000006 (TK00000006)
('PN00000011', '2025-09-10 08:00:00', '2025-09-10 12:00:00', 'TK00000006'),
('PN00000012', '2025-09-12 13:00:00', '2025-09-12 17:00:00', 'TK00000006'),

-- NV00000007 (TK00000007)
('PN00000013', '2025-09-13 08:00:00', '2025-09-13 12:00:00', 'TK00000007'),
('PN00000014', '2025-09-15 13:00:00', '2025-09-15 17:00:00', 'TK00000007'),

-- NV00000008 (TK00000008)
('PN00000015', '2025-09-14 08:00:00', '2025-09-14 12:00:00', 'TK00000008'),
('PN00000016', '2025-09-16 13:00:00', '2025-09-16 17:00:00', 'TK00000008'),

-- NV00000009 (TK00000009 - quản lý)
('PN00000017', '2025-09-17 08:00:00', '2025-09-17 12:00:00', 'TK00000009'),
('PN00000018', '2025-09-19 13:00:00', '2025-09-19 17:00:00', 'TK00000009'),

-- NV00000010 (TK00000010 - quản lý)
('PN00000019', '2025-09-18 08:00:00', '2025-09-18 12:00:00', 'TK00000010'),
('PN00000020', '2025-09-20 13:00:00', '2025-09-20 17:00:00', 'TK00000010');


--GiaPhong
INSERT INTO GiaPhong (ma_gia_phong, gia_ngay_moi, gia_gio_moi, ma_loai_phong, ma_phien_dang_nhap)
VALUES
-- LP00000001: Phòng 1 giường đơn (Thường)
('GP00000001', 300000, 50000, 'LP00000001', 'PN00000001'),

-- LP00000002: Phòng 1 giường đôi (Thường)
('GP00000002', 450000, 70000, 'LP00000002', 'PN00000003'),

-- LP00000003: Phòng 2 giường đôi (Thường)
('GP00000003', 600000, 100000, 'LP00000003', 'PN00000006'),

-- LP00000004: Phòng 1 giường đơn (Vip)
('GP00000004', 500000, 80000, 'LP00000004', 'PN00000008'),

-- LP00000005: Phòng 1 giường đôi (Vip)
('GP00000005', 700000, 120000, 'LP00000005', 'PN00000010'),

-- LP00000006: Phòng 2 giường đôi (Vip)
('GP00000006', 900000, 150000, 'LP00000006', 'PN00000011');


--KhachHang
INSERT INTO KhachHang (ma_khach_hang, CCCD, ten_khach_hang, so_dien_thoai)
VALUES
('KH00000001', N'079123456789', N'Nguyễn Văn A', '0912345678'),
('KH00000002', N'079987654321', N'Trần Thị B', '0987654321'),
('KH00000003', N'079456789123', N'Lê Văn C', '0905123456'),
('KH00000004', N'079321654987', N'Phạm Thị D', '0934567890'),
('KH00000005', N'079741258963', N'Huỳnh Văn E', '0978123456');


--RF
INSERT INTO DonDatPhong 
(ma_don_dat_phong, mo_ta, tg_nhan_phong, tg_tra_phong, tong_tien_du_tinh, tien_dat_coc, da_dat_truoc, ma_khach_hang, ma_phien_dang_nhap)
VALUES
('DP00000001', N'Đặt trước qua điện thoại', '2025-09-05', '2025-09-07', 1200000, 500000, 1, 'KH00000001', 'PN00000001'),
('DP00000002', N'Khách quen', '2025-09-06', '2025-09-08', 2000000, 800000, 1, 'KH00000002', 'PN00000002'),
('DP00000003', N'Yêu cầu tầng 2', '2025-09-09', '2025-09-11', 1500000, 600000, 0, 'KH00000003', 'PN00000003'),
('DP00000004', N'Thanh toán khi nhận phòng', '2025-09-10', '2025-09-12', 1800000, 900000, 1, 'KH00000004', 'PN00000004'),
('DP00000005', N'Check-in trễ 22h', '2025-09-12', '2025-09-14', 2200000, 1000000, 0, 'KH00000005', 'PN00000005'),
('DP00000006', N'Đặt online', '2025-09-14', '2025-09-16', 2500000, 1200000, 1, 'KH00000002', 'PN00000006'),
('DP00000007', N'Cần thêm giường phụ', '2025-09-17', '2025-09-19', 2800000, 1400000, 1, 'KH00000001', 'PN00000007'),
('DP00000008', N'Khách công tác', '2025-09-18', '2025-09-20', 3000000, 1500000, 0, 'KH00000005', 'PN00000008'),
('DP00000009', N'Khách đoàn 4 người', '2025-09-20', '2025-09-23', 3500000, 1700000, 1, 'KH00000004', 'PN00000009'),
('DP00000010', N'Khách yêu cầu phòng VIP', '2025-09-22', '2025-09-25', 4000000, 2000000, 1, 'KH00000003', 'PN00000010');

--RD
INSERT INTO ChiTietDatPhong (ma_chi_tiet_dat_phong, tg_nhan_phong, tg_tra_phong, ma_don_dat_phong, ma_phong, ma_phien_dang_nhap)
VALUES
('CP00000001', '2025-09-07', '2025-09-05', 'DP00000001', 'PH00000001', 'PN00000001'),
('CP00000002', '2025-09-08', '2025-09-06', 'DP00000002', 'PH00000011', 'PN00000002'),
('CP00000003', '2025-09-11', '2025-09-09', 'DP00000003', 'PH00000021', 'PN00000003'),
('CP00000004', '2025-09-12', '2025-09-10', 'DP00000004', 'PH00000031', 'PN00000004'),
('CP00000005', '2025-09-14', '2025-09-12', 'DP00000005', 'PH00000041', 'PN00000005'),
('CP00000006', '2025-09-16', '2025-09-14', 'DP00000006', 'PH00000051', 'PN00000006'),
('CP00000007', '2025-09-19', '2025-09-17', 'DP00000007', 'PH00000002', 'PN00000007'),
('CP00000008', '2025-09-20', '2025-09-18', 'DP00000008', 'PH00000012', 'PN00000008'),
('CP00000009', '2025-09-23', '2025-09-20', 'DP00000009', 'PH00000022', 'PN00000009'),
('CP00000010', '2025-09-25', '2025-09-22', 'DP00000010', 'PH00000032', 'PN00000010');


--Check in check out
INSERT INTO LichSuDiVao (ma_lich_su_di_vao, la_lan_dau_tien, ma_chi_tiet_dat_phong)
VALUES
('LV00000001',  1, 'CP00000001'),
('LV00000002',  1, 'CP00000002'),
('LV00000003',  1, 'CP00000003'),
('LV00000004',  1, 'CP00000004'),
('LV00000005',  1, 'CP00000005'),
('LV00000006',  1, 'CP00000006'),
('LV00000007',  1, 'CP00000007'),
('LV00000008',  1, 'CP00000008'),
('LV00000009',  1, 'CP00000009'),
('LV00000010',  1, 'CP00000010');

-- Lịch sử CheckOut
INSERT INTO LichSuRaNgoai (ma_lich_su_ra_ngoai, la_lan_cuoi_cung, ma_chi_tiet_dat_phong)
VALUES
('LN00000001',  1, 'CP00000001'),
('LN00000002',  1, 'CP00000002'),
('LN00000003',  1, 'CP00000003'),
('LN00000004',  1, 'CP00000004'),
('LN00000005',  1, 'CP00000005'),
('LN00000006',  1, 'CP00000006'),
('LN00000007',  1, 'CP00000007'),
('LN00000008',  1, 'CP00000008'),
('LN00000009',  1, 'CP00000009'),
('LN00000010',  1, 'CP00000010');

--Invoicec
INSERT INTO HoaDon (ma_hoa_don, phuong_thuc_thanh_toan, kieu_hoa_don, tinh_trang_thanh_toan, ma_phien_dang_nhap, ma_don_dat_phong, ma_khach_hang)
VALUES
('HD00000001', N'Tiền mặt',     N'Đặt phòng', N'Đã thanh toán',     'PN00000001', 'DP00000001', 'KH00000001'),
('HD00000002', N'Thẻ tín dụng', N'Đặt phòng', N'Đã thanh toán',     'PN00000002', 'DP00000002', 'KH00000002'),
('HD00000003', N'Chuyển khoản', N'Đặt phòng', N'Đã thanh toán',  'PN00000003', 'DP00000003', 'KH00000003'),
('HD00000004', N'Tiền mặt',     N'Đặt phòng', N'Đã thanh toán',     'PN00000004', 'DP00000004', 'KH00000004'),
('HD00000005', N'Tiền mặt',     N'Đặt phòng', N'Đã thanh toán',  'PN00000005', 'DP00000005', 'KH00000005'),
('HD00000006', N'Thẻ tín dụng', N'Đặt phòng', N'Đã thanh toán',     'PN00000006', 'DP00000006', 'KH00000002'),
('HD00000007', N'Chuyển khoản', N'Đặt phòng', N'Đã thanh toán',     'PN00000007', 'DP00000007', 'KH00000001'),
('HD00000008', N'Tiền mặt',     N'Đặt phòng', N'Đã thanh toán',  'PN00000008', 'DP00000008', 'KH00000005'),
('HD00000009', N'Tiền mặt',     N'Đặt phòng', N'Đã thanh toán',     'PN00000009', 'DP00000009', 'KH00000004'),
('HD00000010', N'Thẻ tín dụng', N'Đặt phòng', N'Đã thanh toán',     'PN00000010', 'DP00000010', 'KH00000003');

INSERT INTO LoaiDichVu (ma_loai_dich_vu, ten_loai_dich_vu)
VALUES
    ('LDV00000001', N'Chăm sóc cá nhân'),
    ('LDV00000002', N'Ăn uống'),
    ('LDV00000003', N'Giặt ủi'),
    ('LDV00000004', N'Vận chuyển và du lịch');

INSERT INTO DichVu (ma_dich_vu, ten_dich_vu, ton_kho, co_the_tang, ma_loai_dich_vu)
VALUES
('DV00000001', N'Gội đầu', 0, 0, 'LDV00000001'),
('DV00000002', N'Massage', 0, 0, 'LDV00000001'),
('DV00000003', N'Ăn sáng', 100, 1, 'LDV00000002'),
('DV00000004', N'Ăn trưa', 100, 1, 'LDV00000002'),
('DV00000005', N'Ăn tối', 100, 1, 'LDV00000002'),
('DV00000006', N'Giặt ủi', 50, 1, 'LDV00000003'),
('DV00000007', N'Đưa đón sân bay', 0, 0, 'LDV00000004'),
('DV00000008', N'Tour du lịch', 0, 0, 'LDV00000004'),
('DV00000009', N'Thuê xe máy', 20, 1, 'LDV00000004'),
('DV00000010', N'Thuê ô tô', 10, 1, 'LDV00000004');

INSERT INTO GiaDichVu (ma_gia_dich_vu, gia_cu, gia_moi, ma_phien_dang_nhap, ma_dich_vu)
VALUES
('GDV00000001', null, 250000, 'PN00000001', 'DV00000001'), -- Gội đầu
('GDV00000002', null, 600000, 'PN00000002', 'DV00000002'), -- Massage
('GDV00000003', null, 120000, 'PN00000003', 'DV00000003'), -- Ăn sáng
('GDV00000004', null, 250000, 'PN00000004', 'DV00000004'), -- Ăn trưa
('GDV00000005', null, 250000, 'PN00000005', 'DV00000005'), -- Ăn tối
('GDV00000006', null, 180000, 'PN00000006', 'DV00000006'), -- Giặt ủi
('GDV00000007', null, 350000, 'PN00000007', 'DV00000007'), -- Đưa đón sân bay
('GDV00000008', null, 1200000, 'PN00000008', 'DV00000008'), -- Tour du lịch
('GDV00000009', null, 250000, 'PN00000009', 'DV00000009'), -- Thuê xe máy
('GDV00000010', null, 600000, 'PN00000010', 'DV00000010'); -- Thuê ô tô


-- Thêm dữ liệu vào bảng NoiThat (các món nội thất thường có trong khách sạn)
-- Tôi chọn một số nội thất cơ bản và cao cấp: giường, bàn làm việc, ghế sofa, tủ quần áo, TV, tủ lạnh mini, điều hòa, đèn ngủ, rèm cửa, gương, két sắt, máy sấy tóc, bàn trà, ghế đôn.
-- Mô tả đơn giản hoặc để trống nếu không cần.

INSERT INTO NoiThat (ma_noi_that, ten_noi_that, mo_ta)
VALUES
    ('NT00000001', N'Giường đơn', N'Giường kích thước 1m2 x 2m'),
    ('NT00000002', N'Giường đôi', N'Giường kích thước 1m6 x 2m'),
    ('NT00000003', N'Bàn làm việc', N'Bàn gỗ với đèn đọc sách'),
    ('NT00000004', N'Ghế sofa', N'Ghế sofa đơn giản'),
    ('NT00000005', N'Tủ quần áo', N'Tủ gỗ 2 cánh'),
    ('NT00000006', N'TV màn hình phẳng', N'TV LED 32 inch'),
    ('NT00000007', N'Tủ lạnh mini', N'Tủ lạnh nhỏ dung tích 50L'),
    ('NT00000008', N'Điều hòa', N'Điều hòa inverter 1 chiều'),
    ('NT00000009', N'Đèn ngủ', N'Đèn đầu giường'),
    ('NT00000010', N'Rèm cửa', N'Rèm che nắng 2 lớp'),
    ('NT00000011', N'Gương soi toàn thân', N'Gương lớn treo tường'),
    ('NT00000012', N'Két sắt', N'Két sắt điện tử an toàn'),
    ('NT00000013', N'Máy sấy tóc', N'Máy sấy tóc treo tường'),
    ('NT00000014', N'Bàn trà', N'Bàn trà nhỏ với ấm đun nước'),
    ('NT00000015', N'Ghế đôn', N'Ghế đôn nhỏ cho phòng');

-- Cho LP00000001: Phòng thường 1 giường đơn (cơ bản)
INSERT INTO NoiThatTrongLoaiPhong (ma_noi_that_trong_loai_phong, so_luong, ma_loai_phong, ma_noi_that)
VALUES
    ('NP00000001', 1, 'LP00000001', 'NT00000001'),  -- Giường đơn
    ('NP00000002', 1, 'LP00000001', 'NT00000003'),  -- Bàn làm việc
    ('NP00000003', 1, 'LP00000001', 'NT00000005'),  -- Tủ quần áo
    ('NP00000004', 1, 'LP00000001', 'NT00000006'),  -- TV
    ('NP00000005', 1, 'LP00000001', 'NT00000008'),  -- Điều hòa
    ('NP00000006', 1, 'LP00000001', 'NT00000009'),  -- Đèn ngủ
    ('NP00000007', 1, 'LP00000001', 'NT00000010'),  -- Rèm cửa
    ('NP00000008', 1, 'LP00000001', 'NT00000011');  -- Gương

-- Cho LP00000002: Phòng thường 1 giường đôi (thêm ghế sofa nhỏ)
INSERT INTO NoiThatTrongLoaiPhong (ma_noi_that_trong_loai_phong, so_luong, ma_loai_phong, ma_noi_that)
VALUES
    ('NP00000009', 1, 'LP00000002', 'NT00000002'),  -- Giường đôi
    ('NP00000010', 1, 'LP00000002', 'NT00000003'),  -- Bàn làm việc
    ('NP00000011', 1, 'LP00000002', 'NT00000004'),  -- Ghế sofa (1 cái cho 2 người)
    ('NP00000012', 1, 'LP00000002', 'NT00000005'),  -- Tủ quần áo
    ('NP00000013', 1, 'LP00000002', 'NT00000006'),  -- TV
    ('NP00000014', 1, 'LP00000002', 'NT00000008'),  -- Điều hòa
    ('NP00000015', 2, 'LP00000002', 'NT00000009'),  -- Đèn ngủ (2 cái)
    ('NP00000016', 1, 'LP00000002', 'NT00000010'),  -- Rèm cửa
    ('NP00000017', 1, 'LP00000002', 'NT00000011');  -- Gương

-- Cho LP00000003: Phòng thường 2 giường đôi (thêm đồ cho 4 người)
INSERT INTO NoiThatTrongLoaiPhong (ma_noi_that_trong_loai_phong, so_luong, ma_loai_phong, ma_noi_that)
VALUES
    ('NP00000018', 2, 'LP00000003', 'NT00000002'),  -- Giường đôi (2 cái)
    ('NP00000019', 1, 'LP00000003', 'NT00000003'),  -- Bàn làm việc
    ('NP00000020', 2, 'LP00000003', 'NT00000004'),  -- Ghế sofa (2 cái)
    ('NP00000021', 2, 'LP00000003', 'NT00000005'),  -- Tủ quần áo (2 cái)
    ('NP00000022', 1, 'LP00000003', 'NT00000006'),  -- TV
    ('NP00000023', 1, 'LP00000003', 'NT00000008'),  -- Điều hòa
    ('NP00000024', 4, 'LP00000003', 'NT00000009'),  -- Đèn ngủ (4 cái)
    ('NP00000025', 1, 'LP00000003', 'NT00000010'),  -- Rèm cửa
    ('NP00000026', 1, 'LP00000003', 'NT00000011');  -- Gương

-- Cho LP00000004: Phòng VIP 1 giường đơn (thêm tủ lạnh, két sắt, máy sấy, bàn trà)
INSERT INTO NoiThatTrongLoaiPhong (ma_noi_that_trong_loai_phong, so_luong, ma_loai_phong, ma_noi_that)
VALUES
    ('NP00000027', 1, 'LP00000004', 'NT00000001'),  -- Giường đơn
    ('NP00000028', 1, 'LP00000004', 'NT00000003'),  -- Bàn làm việc
    ('NP00000029', 1, 'LP00000004', 'NT00000005'),  -- Tủ quần áo
    ('NP00000030', 1, 'LP00000004', 'NT00000006'),  -- TV
    ('NP00000031', 1, 'LP00000004', 'NT00000007'),  -- Tủ lạnh mini
    ('NP00000032', 1, 'LP00000004', 'NT00000008'),  -- Điều hòa
    ('NP00000033', 1, 'LP00000004', 'NT00000009'),  -- Đèn ngủ
    ('NP00000034', 1, 'LP00000004', 'NT00000010'),  -- Rèm cửa
    ('NP00000035', 1, 'LP00000004', 'NT00000011'),  -- Gương
    ('NP00000036', 1, 'LP00000004', 'NT00000012'),  -- Két sắt
    ('NP00000037', 1, 'LP00000004', 'NT00000013'),  -- Máy sấy tóc
    ('NP00000038', 1, 'LP00000004', 'NT00000014');  -- Bàn trà

-- Cho LP00000005: Phòng VIP 1 giường đôi (tương tự VIP đơn nhưng thêm ghế)
INSERT INTO NoiThatTrongLoaiPhong (ma_noi_that_trong_loai_phong, so_luong, ma_loai_phong, ma_noi_that)
VALUES
    ('NP00000039', 1, 'LP00000005', 'NT00000002'),  -- Giường đôi
    ('NP00000040', 1, 'LP00000005', 'NT00000003'),  -- Bàn làm việc
    ('NP00000041', 1, 'LP00000005', 'NT00000004'),  -- Ghế sofa
    ('NP00000042', 1, 'LP00000005', 'NT00000005'),  -- Tủ quần áo
    ('NP00000043', 1, 'LP00000005', 'NT00000006'),  -- TV
    ('NP00000044', 1, 'LP00000005', 'NT00000007'),  -- Tủ lạnh mini
    ('NP00000045', 1, 'LP00000005', 'NT00000008'),  -- Điều hòa
    ('NP00000046', 2, 'LP00000005', 'NT00000009'),  -- Đèn ngủ (2)
    ('NP00000047', 1, 'LP00000005', 'NT00000010'),  -- Rèm cửa
    ('NP00000048', 1, 'LP00000005', 'NT00000011'),  -- Gương
    ('NP00000049', 1, 'LP00000005', 'NT00000012'),  -- Két sắt
    ('NP00000050', 1, 'LP00000005', 'NT00000013'),  -- Máy sấy tóc
    ('NP00000051', 1, 'LP00000005', 'NT00000014');  -- Bàn trà

-- Cho LP00000006: Phòng VIP 2 giường đôi (thêm đồ cho nhiều người, thêm ghế đôn)
INSERT INTO NoiThatTrongLoaiPhong (ma_noi_that_trong_loai_phong, so_luong, ma_loai_phong, ma_noi_that)
VALUES
    ('NP00000052', 2, 'LP00000006', 'NT00000002'),  -- Giường đôi (2)
    ('NP00000053', 1, 'LP00000006', 'NT00000003'),  -- Bàn làm việc
    ('NP00000054', 2, 'LP00000006', 'NT00000004'),  -- Ghế sofa (2)
    ('NP00000055', 2, 'LP00000006', 'NT00000005'),  -- Tủ quần áo (2)
    ('NP00000056', 1, 'LP00000006', 'NT00000006'),  -- TV
    ('NP00000057', 1, 'LP00000006', 'NT00000007'),  -- Tủ lạnh mini
    ('NP00000058', 1, 'LP00000006', 'NT00000008'),  -- Điều hòa
    ('NP00000059', 4, 'LP00000006', 'NT00000009'),  -- Đèn ngủ (4)
    ('NP00000060', 1, 'LP00000006', 'NT00000010'),  -- Rèm cửa
    ('NP00000061', 1, 'LP00000006', 'NT00000011'),  -- Gương
    ('NP00000062', 1, 'LP00000006', 'NT00000012'),  -- Két sắt
    ('NP00000063', 1, 'LP00000006', 'NT00000013'),  -- Máy sấy tóc
    ('NP00000064', 1, 'LP00000006', 'NT00000014'),  -- Bàn trà
    ('NP00000065', 2, 'LP00000006', 'NT00000015');  -- Ghế đôn (2)


INSERT INTO PhuPhi(ma_phu_phi, ten_phu_phi)
values
('PP00000001', N'Check-out trễ'),
('PP00000002', N'Check-in sớm'),
('PP00000003', N'Thuế giá trị gia tăng'),
('PP00000004', N'Đổi phòng')


INSERT INTO GiaPhuPhi(ma_gia_phu_phi,gia_hien_tai,la_phan_tram ,ma_phien_dang_nhap, ma_phu_phi)
values
('GP00000001', '200',1 , 'PN00000002', 'PP00000001'),
('GP00000002', '100000',0 , 'PN00000002', 'PP00000002'),
('GP00000003', '10',1 , 'PN00000002', 'PP00000003'),
('GP00000004', '100000',0 , 'PN00000002', 'PP00000004')


--Tới hạn trả phòng
INSERT INTO DonDatPhong 
(ma_don_dat_phong, mo_ta, tg_nhan_phong, tg_tra_phong, tong_tien_du_tinh, tien_dat_coc, da_dat_truoc, ma_khach_hang, ma_phien_dang_nhap, thoi_gian_tao)
VALUES
('DP00000011', N'Đặt trước qua điện thoại', dateadd(day, -2, getdate()), dateadd(minute, 1, getdate()), 1200000, 0, 1, 'KH00000001', 'PN00000001', dateadd(day, -2, getdate()))
INSERT INTO ChiTietDatPhong (ma_chi_tiet_dat_phong, tg_nhan_phong, tg_tra_phong, ma_don_dat_phong, ma_phong, ma_phien_dang_nhap, thoi_gian_tao)
VALUES
('CP00000011', dateadd(day, -2, getdate()),dateadd(minute, 1, getdate()), 'DP00000011', 'PH00000001', 'PN00000001', dateadd(day, -2, getdate()))
INSERT INTO LichSuDiVao (ma_lich_su_di_vao, la_lan_dau_tien, ma_chi_tiet_dat_phong, thoi_gian_tao)
VALUES
('LV00000011',  1, 'CP00000011', dateadd(day, -2, getdate()))
insert into CongViec(ma_cong_viec, ten_trang_thai, tg_bat_dau, tg_ket_thuc, da_xoa, thoi_gian_tao, ma_phong)
values ('CV00000011', N'SỬ DỤNG', dateadd(day, -2, getdate()), dateadd(minute, 1, getdate()),0, dateadd(day, -2, getdate()),'PH00000001')

--kiểm tra - > sử dụng
INSERT INTO DonDatPhong 
(ma_don_dat_phong, mo_ta, tg_nhan_phong, tg_tra_phong, tong_tien_du_tinh, tien_dat_coc, da_dat_truoc, ma_khach_hang, ma_phien_dang_nhap, thoi_gian_tao)
VALUES
('DP00000012', N'Đặt trước qua điện thoại', dateadd(minute, -29, getdate()), dateadd(day, 2, getdate()), 1200000, 0, 1, 'KH00000001', 'PN00000001', dateadd(day, -2, getdate()))
INSERT INTO ChiTietDatPhong (ma_chi_tiet_dat_phong, tg_nhan_phong, tg_tra_phong, ma_don_dat_phong, ma_phong, ma_phien_dang_nhap, thoi_gian_tao)
VALUES
('CP00000012',dateadd(minute, -29, getdate()), dateadd(day,2 , getdate()), 'DP00000012', 'PH00000002', 'PN00000001', dateadd(day, -2, getdate()))
INSERT INTO LichSuDiVao (ma_lich_su_di_vao, la_lan_dau_tien, ma_chi_tiet_dat_phong, thoi_gian_tao)
VALUES
('LV00000012',  1, 'CP00000012',dateadd(minute, -29, getdate()))
insert into CongViec(ma_cong_viec, ten_trang_thai, tg_bat_dau, tg_ket_thuc, da_xoa, thoi_gian_tao, ma_phong)
values ('CV00000012', N'KIỂM TRA', dateadd(minute, -29, getdate()), dateadd(minute, 1, getdate()),0, dateadd(minute, -29, getdate()),'PH00000002')


--trễ - > trống
INSERT INTO DonDatPhong 
(ma_don_dat_phong, mo_ta, tg_nhan_phong, tg_tra_phong, tong_tien_du_tinh, tien_dat_coc, da_dat_truoc, ma_khach_hang, ma_phien_dang_nhap, thoi_gian_tao, loai)
VALUES
('DP00000013', N'Đặt trước qua điện thoại', dateadd(day, -2, getdate()), dateadd(minute, -59, getdate()), 1200000, 0, 1, 'KH00000001', 'PN00000001',dateadd(day, -2, getdate()), N'ĐẶT NHIỀU')
INSERT INTO ChiTietDatPhong (ma_chi_tiet_dat_phong, tg_nhan_phong, tg_tra_phong, ma_don_dat_phong, ma_phong, ma_phien_dang_nhap, thoi_gian_tao)
VALUES
('CP00000013', dateadd(day, -2, getdate()),dateadd(minute, -59, getdate()), 'DP00000013', 'PH00000003', 'PN00000001',dateadd(day, -2, getdate())),
('CP00000014', dateadd(day, -2, getdate()),dateadd(minute, -59, getdate()), 'DP00000013', 'PH00000004', 'PN00000001',dateadd(day, -2, getdate())),
('CP00000015', dateadd(day, -2, getdate()),dateadd(minute, -59, getdate()), 'DP00000013', 'PH00000005', 'PN00000001',dateadd(day, -2, getdate()))
INSERT INTO LichSuDiVao (ma_lich_su_di_vao, la_lan_dau_tien, ma_chi_tiet_dat_phong, thoi_gian_tao)
VALUES
('LV00000013',  1, 'CP00000013', dateadd(day, -2, getdate())),
('LV00000014',  1, 'CP00000014', dateadd(day, -2, getdate())),
('LV00000015',  1, 'CP00000015', dateadd(day, -2, getdate()))
insert into CongViec(ma_cong_viec, ten_trang_thai, tg_bat_dau, tg_ket_thuc, da_xoa, thoi_gian_tao, ma_phong)
values('CV00000013', N'CHECKOUT TRỄ', dateadd(minute, -59, getdate()), dateadd(minute, 1, getdate()),0, dateadd(minute, -59, getdate()),'PH00000003'),
('CV00000014', N'CHECKOUT TRỄ', dateadd(minute, -59, getdate()), dateadd(minute, 1, getdate()),0, dateadd(minute, -59, getdate()),'PH00000004'),
('CV00000015', N'CHECKOUT TRỄ', dateadd(minute, -59, getdate()), dateadd(minute, 1, getdate()),0, dateadd(minute, -59, getdate()),'PH00000005')




--Tới hạn check-in
INSERT INTO DonDatPhong 
(ma_don_dat_phong, mo_ta, tg_nhan_phong, tg_tra_phong, tong_tien_du_tinh, tien_dat_coc, da_dat_truoc, ma_khach_hang, ma_phien_dang_nhap, thoi_gian_tao)
VALUES
('DP00000014', N'Đặt trước qua điện thoại', dateadd(second, 35, getdate()), dateadd(day, 1, getdate()), 1200000, 0, 1, 'KH00000002', 'PN00000002', dateadd(day, -2, getdate()))
INSERT INTO ChiTietDatPhong (ma_chi_tiet_dat_phong, tg_nhan_phong, tg_tra_phong, ma_don_dat_phong, ma_phong, ma_phien_dang_nhap, thoi_gian_tao)
VALUES
('CP00000016',dateadd(second, 35, getdate()),dateadd(day, 1, getdate()), 'DP00000014', 'PH00000006', 'PN00000002',dateadd(day, -2, getdate()))
insert into CongViec(ma_cong_viec, ten_trang_thai, tg_bat_dau, tg_ket_thuc, da_xoa, thoi_gian_tao, ma_phong)
values ('CV00000016', N'CHỜ CHECKIN',dateadd(second, 35, getdate()), dateadd(hour, 12, getdate()),0, dateadd(day, -2, getdate()),'PH00000006')


INSERT INTO DonDatPhong 
(ma_don_dat_phong, mo_ta, tg_nhan_phong, tg_tra_phong, tong_tien_du_tinh, tien_dat_coc, da_dat_truoc, ma_khach_hang, ma_phien_dang_nhap, thoi_gian_tao, loai)
VALUES
('DP00000015', N'Đặt trước qua điện thoại', dateadd(minute, 3, getdate()), dateadd(day, 1, getdate()), 1200000, 0, 1, 'KH00000003', 'PN00000003', dateadd(day, -2, getdate()),N'ĐẶT NHIỀU')
INSERT INTO ChiTietDatPhong (ma_chi_tiet_dat_phong, tg_nhan_phong, tg_tra_phong, ma_don_dat_phong, ma_phong, ma_phien_dang_nhap, thoi_gian_tao)
VALUES
('CP00000017',dateadd(minute, 3, getdate()),dateadd(day, 1, getdate()), 'DP00000015', 'PH00000007', 'PN00000002',dateadd(day, -2, getdate())),
('CP00000018',dateadd(minute, 3, getdate()),dateadd(day, 1, getdate()), 'DP00000015', 'PH00000008', 'PN00000002',dateadd(day, -2, getdate()))
insert into CongViec(ma_cong_viec, ten_trang_thai, tg_bat_dau, tg_ket_thuc, da_xoa, thoi_gian_tao, ma_phong)
values ('CV00000017', N'CHỜ CHECKIN', dateadd(minute, 3, getdate()), dateadd(hour, 12, getdate()),0, dateadd(day, -2, getdate()),'PH00000007'),
('CV00000018', N'CHỜ CHECKIN', dateadd(minute, 3, getdate()), dateadd(hour, 12, getdate()),0, dateadd(day, -2, getdate()),'PH00000008')
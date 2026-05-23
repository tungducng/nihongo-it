# 06 — Admin content management

> **Project**: `admin`
> **External services**: `none`

## Pre-conditions

- `setup` project has run; admin user has ROLE_ADMIN.

## Test cases

### TC-06-01: Admin vocabulary list renders (@smoke)

- **Route**: `/vocabulary`
- **Assertions**: URL not `/login`; heading "Từ vựng" visible.

### TC-06-02: Admin categories list renders

- **Route**: `/categories`
- **Assertions**: URL not `/login`; heading "Danh mục" visible.

### TC-06-03: Admin topics list renders

- **Route**: `/topics`
- **Assertions**: URL not `/login`; heading "Chủ đề" visible.

### TC-06-04: Admin conversations list renders

- **Route**: `/conversations`
- **Assertions**: URL not `/login`; heading "Hội thoại" visible.

### TC-06-05: Admin statistics overview renders

- **Route**: `/statistics`
- **Assertions**: URL not `/login`; heading "Thống kê" visible.

## Notes for the Generator

- File: `e2e/tests/admin/content.spec.ts`. admin-project.

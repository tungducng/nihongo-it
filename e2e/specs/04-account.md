# 04 — Account + Profile (user app)

> **Project**: `user`
> **External services**: `none`

## Pre-conditions

- `setup` project has run.

## Test cases

### TC-04-01: Profile page renders (@smoke)

- **Route**: `/profile`
- **Assertions**: URL not `/login`; card title "Hồ sơ cá nhân" visible.

### TC-04-02: Change-password page renders

- **Route**: `/account/change-password`
- **Assertions**: URL not `/login`; card title "Đổi mật khẩu" visible.

### TC-04-03: Speech analyzer page renders

- **Route**: `/speech`
- **Assertions**: URL not `/login`; heading "Phân tích phát âm" visible.
- **Note**: Recording + actual analysis require Python NLP service; covered
  here is only the page-render path.

## Notes for the Generator

- File: `e2e/tests/user/account.spec.ts`. user-project.

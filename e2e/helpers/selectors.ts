// Single source of truth for data-testid selectors.
//
// CONVENTION:
//   data-testid="<area>-<element>[-<modifier>]"
//   e.g. "vocab-list-item", "vocab-list-item-save", "admin-user-row"
//
// PREFER role+name FIRST (getByRole, getByLabel). Only add an entry here when
// a target has no good semantic role or is ambiguous in a list.

export const SEL = {
  auth: {
    emailInput: 'input[type="email"]',
    passwordInput: 'input[type="password"]',
    submitBtn: 'button[type="submit"]',
  },
  // Placeholders — populated as POMs grow.
  vocab: {
    listItem: '[data-testid="vocab-list-item"]',
  },
  flashcard: {
    studyCard: '[data-testid="flashcard-study-card"]',
    ratingBtn: (rating: 1 | 2 | 3 | 4) => `[data-testid="flashcard-rating-${rating}"]`,
  },
  admin: {
    userRow: '[data-testid="admin-user-row"]',
  },
} as const

import uuid from 'react-native-uuid';

/** Maximum character length for task title */
export const MAX_TITLE_LENGTH = 100;

/** Maximum character length for task description */
export const MAX_DESCRIPTION_LENGTH = 500;

/**
 * Generates a new UUID v4 string.
 * @returns {string} UUID v4
 */
export const generateId = () => uuid.v4();

/**
 * Returns the current time as an ISO 8601 string.
 * @returns {string} ISO timestamp
 */
export const generateTimestamp = () => new Date().toISOString();

/**
 * Validates a task object and returns an object with `valid` and `errors`.
 * @param {{ title?: string, location?: string, description?: string }} task
 * @returns {{ valid: boolean, errors: string[] }}
 */
export const validateTask = (task) => {
  const errors = [];

  if (!task.title || task.title.trim().length === 0) {
    errors.push('Title is required.');
  } else if (task.title.trim().length > MAX_TITLE_LENGTH) {
    errors.push(`Title must be ${MAX_TITLE_LENGTH} characters or less.`);
  }

  if (!task.location || task.location.trim().length === 0) {
    errors.push('Location is required.');
  }

  if (task.description && task.description.length > MAX_DESCRIPTION_LENGTH) {
    errors.push(`Description must be ${MAX_DESCRIPTION_LENGTH} characters or less.`);
  }

  return { valid: errors.length === 0, errors };
};

/**
 * Compares two location strings case-insensitively after trimming whitespace.
 * @param {string} a
 * @param {string} b
 * @returns {boolean}
 */
export const locationsMatch = (a, b) => {
  if (!a || !b) return false;
  return a.trim().toLowerCase() === b.trim().toLowerCase();
};

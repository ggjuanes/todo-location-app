import AsyncStorage from '@react-native-async-storage/async-storage';

const STORAGE_KEY = '@location_todo_tasks';

/**
 * Loads all tasks from AsyncStorage.
 * @returns {Promise<Array>} Array of task objects
 */
export const getTasks = async () => {
  try {
    const json = await AsyncStorage.getItem(STORAGE_KEY);
    return json ? JSON.parse(json) : [];
  } catch (error) {
    console.error('storageService.getTasks failed:', error);
    throw new Error('Failed to load tasks from storage.');
  }
};

/**
 * Persists the full tasks array to AsyncStorage.
 * @param {Array} tasks
 */
export const saveTasks = async (tasks) => {
  try {
    await AsyncStorage.setItem(STORAGE_KEY, JSON.stringify(tasks));
  } catch (error) {
    console.error('storageService.saveTasks failed:', error);
    throw new Error('Failed to save tasks to storage.');
  }
};

/**
 * Adds a new task and persists.
 * @param {Object} task - Full task object (with id, timestamps, etc.)
 * @returns {Promise<Array>} Updated tasks array
 */
export const addTask = async (task) => {
  const tasks = await getTasks();
  const updated = [task, ...tasks];
  await saveTasks(updated);
  return updated;
};

/**
 * Updates an existing task by id and persists.
 * @param {string} id
 * @param {Object} updates - Partial task fields to merge
 * @returns {Promise<Array>} Updated tasks array
 */
export const updateTask = async (id, updates) => {
  const tasks = await getTasks();
  const updated = tasks.map((t) =>
    t.id === id ? { ...t, ...updates } : t
  );
  await saveTasks(updated);
  return updated;
};

/**
 * Deletes a task by id and persists.
 * @param {string} id
 * @returns {Promise<Array>} Updated tasks array
 */
export const deleteTask = async (id) => {
  const tasks = await getTasks();
  const updated = tasks.filter((t) => t.id !== id);
  await saveTasks(updated);
  return updated;
};

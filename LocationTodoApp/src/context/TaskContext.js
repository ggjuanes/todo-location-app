import React, { createContext, useReducer, useContext, useEffect, useCallback } from 'react';
import * as storageService from '../services/storageService';
import { generateId, generateTimestamp } from '../utils/helpers';

const TaskContext = createContext();

const initialState = {
  tasks: [],
  loading: true,
  error: null,
};

const ACTION = {
  SET_TASKS: 'SET_TASKS',
  SET_LOADING: 'SET_LOADING',
  SET_ERROR: 'SET_ERROR',
};

function taskReducer(state, action) {
  switch (action.type) {
    case ACTION.SET_TASKS:
      return { ...state, tasks: action.payload, loading: false, error: null };
    case ACTION.SET_LOADING:
      return { ...state, loading: action.payload };
    case ACTION.SET_ERROR:
      return { ...state, error: action.payload, loading: false };
    default:
      return state;
  }
}

/**
 * Provides task state and CRUD actions to the component tree.
 */
export function TaskProvider({ children }) {
  const [state, dispatch] = useReducer(taskReducer, initialState);

  // Load tasks from storage on mount
  useEffect(() => {
    loadTasks();
  }, []);

  const loadTasks = useCallback(async () => {
    dispatch({ type: ACTION.SET_LOADING, payload: true });
    try {
      const tasks = await storageService.getTasks();
      dispatch({ type: ACTION.SET_TASKS, payload: tasks });
    } catch (error) {
      console.error('Failed to load tasks:', error);
      dispatch({ type: ACTION.SET_ERROR, payload: 'Failed to load tasks.' });
    }
  }, []);

  const createTask = useCallback(async ({ title, description, location }) => {
    try {
      const now = generateTimestamp();
      const task = {
        id: generateId(),
        title: title.trim(),
        description: description ? description.trim() : '',
        location: location.trim(),
        status: 'pending',
        createdAt: now,
        updatedAt: now,
      };
      const tasks = await storageService.addTask(task);
      dispatch({ type: ACTION.SET_TASKS, payload: tasks });
      return task;
    } catch (error) {
      console.error('Failed to create task:', error);
      dispatch({ type: ACTION.SET_ERROR, payload: 'Failed to create task.' });
      throw error;
    }
  }, []);

  const updateTask = useCallback(async (id, updates) => {
    try {
      const updatesWithTimestamp = {
        ...updates,
        updatedAt: generateTimestamp(),
      };
      // Trim string fields if present
      if (updatesWithTimestamp.title) updatesWithTimestamp.title = updatesWithTimestamp.title.trim();
      if (updatesWithTimestamp.location) updatesWithTimestamp.location = updatesWithTimestamp.location.trim();
      if (updatesWithTimestamp.description !== undefined) {
        updatesWithTimestamp.description = updatesWithTimestamp.description.trim();
      }
      const tasks = await storageService.updateTask(id, updatesWithTimestamp);
      dispatch({ type: ACTION.SET_TASKS, payload: tasks });
    } catch (error) {
      console.error('Failed to update task:', error);
      dispatch({ type: ACTION.SET_ERROR, payload: 'Failed to update task.' });
      throw error;
    }
  }, []);

  const deleteTask = useCallback(async (id) => {
    try {
      const tasks = await storageService.deleteTask(id);
      dispatch({ type: ACTION.SET_TASKS, payload: tasks });
    } catch (error) {
      console.error('Failed to delete task:', error);
      dispatch({ type: ACTION.SET_ERROR, payload: 'Failed to delete task.' });
      throw error;
    }
  }, []);

  const toggleTaskStatus = useCallback(async (id) => {
    const task = state.tasks.find((t) => t.id === id);
    if (!task) return;
    const newStatus = task.status === 'pending' ? 'completed' : 'pending';
    await updateTask(id, { status: newStatus });
  }, [state.tasks, updateTask]);

  const value = {
    tasks: state.tasks,
    loading: state.loading,
    error: state.error,
    createTask,
    updateTask,
    deleteTask,
    toggleTaskStatus,
    loadTasks,
  };

  return <TaskContext.Provider value={value}>{children}</TaskContext.Provider>;
}

/**
 * Hook to access task context. Must be used within a TaskProvider.
 */
export function useTasks() {
  const context = useContext(TaskContext);
  if (!context) {
    throw new Error('useTasks must be used within a TaskProvider');
  }
  return context;
}

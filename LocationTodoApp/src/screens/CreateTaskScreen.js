import React from 'react';
import { Alert } from 'react-native';
import TaskForm from '../components/TaskForm';
import { useTasks } from '../context/TaskContext';

export default function CreateTaskScreen({ navigation }) {
  const { createTask } = useTasks();

  const handleSubmit = async (values) => {
    try {
      await createTask(values);
      navigation.goBack();
    } catch (error) {
      Alert.alert('Error', 'Failed to create task. Please try again.');
    }
  };

  return (
    <TaskForm
      onSubmit={handleSubmit}
      submitButtonText="Create Task"
      onCancel={() => navigation.goBack()}
    />
  );
}

import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet, Alert } from 'react-native';
import TaskForm from '../components/TaskForm';
import { useTasks } from '../context/TaskContext';

export default function EditTaskScreen({ route, navigation }) {
  const { task } = route.params;
  const { updateTask, deleteTask, toggleTaskStatus } = useTasks();

  const handleSubmit = async (values) => {
    try {
      await updateTask(task.id, values);
      navigation.goBack();
    } catch (error) {
      Alert.alert('Error', 'Failed to update task. Please try again.');
    }
  };

  const handleDelete = () => {
    Alert.alert(
      'Delete Task',
      'Are you sure you want to delete this task?',
      [
        { text: 'Cancel', style: 'cancel' },
        {
          text: 'Delete',
          style: 'destructive',
          onPress: async () => {
            try {
              await deleteTask(task.id);
              navigation.goBack();
            } catch (error) {
              Alert.alert('Error', 'Failed to delete task.');
            }
          },
        },
      ]
    );
  };

  const handleToggleStatus = async () => {
    try {
      await toggleTaskStatus(task.id);
      navigation.goBack();
    } catch (error) {
      Alert.alert('Error', 'Failed to update task status.');
    }
  };

  const isCompleted = task.status === 'completed';

  return (
    <View style={styles.container}>
      <View style={styles.statusRow}>
        <TouchableOpacity style={styles.statusButton} onPress={handleToggleStatus}>
          <View style={[styles.statusDot, isCompleted && styles.completedDot]} />
          <Text style={styles.statusText}>
            {isCompleted ? 'Completed' : 'Pending'} — tap to toggle
          </Text>
        </TouchableOpacity>
      </View>

      <TaskForm
        initialValues={task}
        onSubmit={handleSubmit}
        submitButtonText="Update Task"
        onCancel={() => navigation.goBack()}
      />

      <TouchableOpacity style={styles.deleteButton} onPress={handleDelete}>
        <Text style={styles.deleteText}>Delete Task</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
  },
  statusRow: {
    paddingHorizontal: 16,
    paddingTop: 12,
  },
  statusButton: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#f5f5f5',
    padding: 12,
    borderRadius: 8,
  },
  statusDot: {
    width: 12,
    height: 12,
    borderRadius: 6,
    backgroundColor: '#F5A623',
    marginRight: 8,
  },
  completedDot: {
    backgroundColor: '#4CAF50',
  },
  statusText: {
    fontSize: 14,
    color: '#666',
  },
  deleteButton: {
    margin: 16,
    marginBottom: 40,
    padding: 14,
    borderRadius: 8,
    backgroundColor: '#FEE',
    borderWidth: 1,
    borderColor: '#FCC',
    alignItems: 'center',
  },
  deleteText: {
    color: '#C00',
    fontSize: 16,
    fontWeight: '600',
  },
});

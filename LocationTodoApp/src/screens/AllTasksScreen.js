import React, { useCallback } from 'react';
import {
  View,
  FlatList,
  StyleSheet,
  ActivityIndicator,
  RefreshControl,
} from 'react-native';
import { useTasks } from '../context/TaskContext';
import TaskItem from '../components/TaskItem';
import EmptyState from '../components/EmptyState';

export default function AllTasksScreen({ navigation }) {
  const { tasks, loading, loadTasks, toggleTaskStatus, deleteTask } = useTasks();

  const handlePress = useCallback((task) => {
    navigation.navigate('EditTask', { task });
  }, [navigation]);

  const renderItem = useCallback(({ item }) => (
    <TaskItem
      task={item}
      onToggle={toggleTaskStatus}
      onPress={handlePress}
      onDelete={deleteTask}
    />
  ), [toggleTaskStatus, handlePress, deleteTask]);

  const keyExtractor = useCallback((item) => item.id, []);

  if (loading && tasks.length === 0) {
    return (
      <View style={styles.centered}>
        <ActivityIndicator size="large" color="#4A90D9" />
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <FlatList
        data={tasks}
        renderItem={renderItem}
        keyExtractor={keyExtractor}
        contentContainerStyle={tasks.length === 0 ? styles.emptyList : styles.list}
        refreshControl={
          <RefreshControl refreshing={loading} onRefresh={loadTasks} />
        }
        ListEmptyComponent={
          <EmptyState
            icon="📝"
            title="No tasks yet"
            message="Create your first task to get started!"
            actionLabel="Create Task"
            onAction={() => navigation.navigate('CreateTask')}
          />
        }
        removeClippedSubviews={true}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  centered: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  list: {
    paddingVertical: 8,
  },
  emptyList: {
    flexGrow: 1,
  },
});

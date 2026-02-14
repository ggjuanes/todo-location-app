import React, { useEffect, useCallback, useMemo } from 'react';
import {
  View,
  Text,
  FlatList,
  StyleSheet,
  ActivityIndicator,
  TouchableOpacity,
  Linking,
  Platform,
} from 'react-native';
import { useTasks } from '../context/TaskContext';
import useCurrentLocation from '../hooks/useCurrentLocation';
import TaskItem from '../components/TaskItem';
import EmptyState from '../components/EmptyState';
import { locationsMatch } from '../utils/helpers';

export default function AvailableTasksScreen({ navigation }) {
  const { tasks, toggleTaskStatus, deleteTask } = useTasks();
  const { city, loading, error, permissionDenied, refetch } = useCurrentLocation();

  // Fetch location on initial mount
  useEffect(() => {
    refetch();
  }, [refetch]);

  // Filter tasks matching current city
  const availableTasks = useMemo(() => {
    if (!city) return [];
    return tasks.filter((t) => locationsMatch(t.location, city));
  }, [tasks, city]);

  const handlePress = useCallback((task) => {
    navigation.navigate('EditTask', { task });
  }, [navigation]);

  const openSettings = () => {
    if (Platform.OS === 'ios') {
      Linking.openURL('app-settings:');
    } else {
      Linking.openSettings();
    }
  };

  const renderItem = useCallback(({ item }) => (
    <TaskItem
      task={item}
      onToggle={toggleTaskStatus}
      onPress={handlePress}
      onDelete={deleteTask}
    />
  ), [toggleTaskStatus, handlePress, deleteTask]);

  const keyExtractor = useCallback((item) => item.id, []);

  // Permission denied state
  if (permissionDenied) {
    return (
      <View style={styles.container}>
        <EmptyState
          icon="📍"
          title="Location Permission Required"
          message="Please enable location permissions to see tasks available near you."
          actionLabel="Open Settings"
          onAction={openSettings}
        />
      </View>
    );
  }

  // Loading state
  if (loading) {
    return (
      <View style={styles.centered}>
        <ActivityIndicator size="large" color="#4A90D9" />
        <Text style={styles.loadingText}>Detecting your location...</Text>
      </View>
    );
  }

  // Error state
  if (error) {
    return (
      <View style={styles.container}>
        <EmptyState
          icon="⚠️"
          title="Location Error"
          message={error}
          actionLabel="Retry"
          onAction={refetch}
        />
      </View>
    );
  }

  return (
    <View style={styles.container}>
      {city && (
        <View style={styles.locationBanner}>
          <Text style={styles.locationLabel}>📍 You are in:</Text>
          <Text style={styles.cityName}>{city}</Text>
          <TouchableOpacity onPress={refetch} style={styles.refreshButton}>
            <Text style={styles.refreshText}>↻ Refresh</Text>
          </TouchableOpacity>
        </View>
      )}

      <FlatList
        data={availableTasks}
        renderItem={renderItem}
        keyExtractor={keyExtractor}
        contentContainerStyle={availableTasks.length === 0 ? styles.emptyList : styles.list}
        ListEmptyComponent={
          <EmptyState
            icon="✅"
            title="No tasks here"
            message={`No tasks available in ${city || 'your current location'}.`}
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
    padding: 32,
  },
  loadingText: {
    marginTop: 12,
    fontSize: 16,
    color: '#888',
  },
  locationBanner: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#E8F4FD',
    padding: 12,
    marginHorizontal: 16,
    marginTop: 12,
    marginBottom: 4,
    borderRadius: 10,
  },
  locationLabel: {
    fontSize: 14,
    color: '#555',
    marginRight: 4,
  },
  cityName: {
    fontSize: 16,
    fontWeight: '700',
    color: '#333',
    flex: 1,
  },
  refreshButton: {
    paddingHorizontal: 8,
    paddingVertical: 4,
  },
  refreshText: {
    color: '#4A90D9',
    fontSize: 14,
    fontWeight: '600',
  },
  list: {
    paddingVertical: 8,
  },
  emptyList: {
    flexGrow: 1,
  },
});

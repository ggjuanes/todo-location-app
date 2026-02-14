import React from 'react';
import { TouchableOpacity, Text, StyleSheet } from 'react-native';
import { NavigationContainer } from '@react-navigation/native';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { createNativeStackNavigator } from '@react-navigation/native-stack';

import AllTasksScreen from '../screens/AllTasksScreen';
import AvailableTasksScreen from '../screens/AvailableTasksScreen';
import CreateTaskScreen from '../screens/CreateTaskScreen';
import EditTaskScreen from '../screens/EditTaskScreen';

const Tab = createBottomTabNavigator();
const Stack = createNativeStackNavigator();

/**
 * Stack navigator wrapping the "All Tasks" tab to allow push navigation
 * to Create/Edit screens.
 */
function AllTasksStack() {
  return (
    <Stack.Navigator>
      <Stack.Screen
        name="AllTasksList"
        component={AllTasksScreen}
        options={({ navigation }) => ({
          title: 'All Tasks',
          headerRight: () => (
            <TouchableOpacity
              onPress={() => navigation.navigate('CreateTask')}
              style={styles.headerButton}
            >
              <Text style={styles.headerButtonText}>+ New</Text>
            </TouchableOpacity>
          ),
        })}
      />
      <Stack.Screen
        name="CreateTask"
        component={CreateTaskScreen}
        options={{ title: 'Create Task' }}
      />
      <Stack.Screen
        name="EditTask"
        component={EditTaskScreen}
        options={{ title: 'Edit Task' }}
      />
    </Stack.Navigator>
  );
}

/**
 * Stack navigator wrapping the "Available" tab so it can also push
 * to Create/Edit screens.
 */
function AvailableStack() {
  return (
    <Stack.Navigator>
      <Stack.Screen
        name="AvailableList"
        component={AvailableTasksScreen}
        options={{ title: 'Available Tasks' }}
      />
      <Stack.Screen
        name="CreateTask"
        component={CreateTaskScreen}
        options={{ title: 'Create Task' }}
      />
      <Stack.Screen
        name="EditTask"
        component={EditTaskScreen}
        options={{ title: 'Edit Task' }}
      />
    </Stack.Navigator>
  );
}

export default function AppNavigator() {
  return (
    <NavigationContainer>
      <Tab.Navigator
        screenOptions={{
          headerShown: false,
          tabBarActiveTintColor: '#4A90D9',
          tabBarInactiveTintColor: '#999',
          tabBarStyle: { paddingBottom: 4, height: 56 },
          tabBarLabelStyle: { fontSize: 12, fontWeight: '600' },
        }}
      >
        <Tab.Screen
          name="AllTasks"
          component={AllTasksStack}
          options={{
            tabBarLabel: 'All Tasks',
            tabBarIcon: ({ color, size }) => (
              <Text style={{ fontSize: size, color }}>📋</Text>
            ),
          }}
        />
        <Tab.Screen
          name="Available"
          component={AvailableStack}
          options={{
            tabBarLabel: 'Available',
            tabBarIcon: ({ color, size }) => (
              <Text style={{ fontSize: size, color }}>📍</Text>
            ),
          }}
        />
      </Tab.Navigator>
    </NavigationContainer>
  );
}

const styles = StyleSheet.create({
  headerButton: {
    marginRight: 8,
    paddingHorizontal: 12,
    paddingVertical: 6,
    backgroundColor: '#4A90D9',
    borderRadius: 6,
  },
  headerButtonText: {
    color: '#fff',
    fontWeight: '600',
    fontSize: 14,
  },
});

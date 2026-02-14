import * as Location from 'expo-location';

/**
 * Requests "when in use" location permission.
 * @returns {Promise<'granted'|'denied'|'blocked'>} Permission status
 */
export const requestLocationPermission = async () => {
  try {
    const { status } = await Location.requestForegroundPermissionsAsync();
    if (status === 'granted') return 'granted';
    return 'denied';
  } catch (error) {
    console.error('requestLocationPermission failed:', error);
    return 'denied';
  }
};

/**
 * Checks current location permission without prompting.
 * @returns {Promise<'granted'|'denied'|'undetermined'>}
 */
export const checkLocationPermission = async () => {
  try {
    const { status } = await Location.getForegroundPermissionsAsync();
    return status;
  } catch (error) {
    console.error('checkLocationPermission failed:', error);
    return 'denied';
  }
};

/**
 * Gets current GPS coordinates.
 * @returns {Promise<{ latitude: number, longitude: number }>}
 */
export const getCurrentLocation = async () => {
  try {
    const location = await Location.getCurrentPositionAsync({
      accuracy: Location.Accuracy.Balanced,
    });
    return {
      latitude: location.coords.latitude,
      longitude: location.coords.longitude,
    };
  } catch (error) {
    console.error('getCurrentLocation failed:', error);
    throw new Error('Unable to get current location. Please check GPS settings.');
  }
};

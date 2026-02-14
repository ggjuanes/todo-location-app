import { useState, useCallback } from 'react';
import { requestLocationPermission, getCurrentLocation } from '../services/locationService';
import { reverseGeocode } from '../services/geocodingService';

/**
 * Custom hook that encapsulates the full location flow:
 * permission request → GPS fetch → reverse geocoding.
 *
 * @returns {{ city: string|null, loading: boolean, error: string|null, permissionDenied: boolean, refetch: () => void }}
 */
export default function useCurrentLocation() {
  const [city, setCity] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [permissionDenied, setPermissionDenied] = useState(false);

  const fetchLocation = useCallback(async () => {
    setLoading(true);
    setError(null);
    setPermissionDenied(false);

    try {
      // Step 1: Request permission
      const permission = await requestLocationPermission();
      if (permission !== 'granted') {
        setPermissionDenied(true);
        setLoading(false);
        return;
      }

      // Step 2: Get GPS coordinates
      const coords = await getCurrentLocation();

      // Step 3: Reverse geocode to city name
      const cityName = await reverseGeocode(coords.latitude, coords.longitude);
      setCity(cityName);
    } catch (err) {
      console.error('useCurrentLocation error:', err);
      setError(err.message || 'Failed to determine your location.');
    } finally {
      setLoading(false);
    }
  }, []);

  return { city, loading, error, permissionDenied, refetch: fetchLocation };
}

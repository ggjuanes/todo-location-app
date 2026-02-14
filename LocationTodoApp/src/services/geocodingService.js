import axios from 'axios';

const BASE_URL = 'https://geocode.maps.co/reverse';

// Simple in-memory cache keyed by rounded coordinates
const cache = new Map();

/**
 * Rounds a coordinate to 2 decimal places for cache keying.
 * This groups nearby locations to reduce redundant API calls.
 */
const roundCoord = (val) => Math.round(val * 100) / 100;

/**
 * Extracts the best city/town name from a geocoding response.
 * Priority: city > town > village > county
 * @param {Object} address
 * @returns {string|null}
 */
const extractCityName = (address) => {
  if (!address) return null;
  return address.city || address.town || address.village || address.county || null;
};

/**
 * Performs reverse geocoding to determine the city/town for given coordinates.
 * Implements caching and retry with exponential backoff.
 * @param {number} latitude
 * @param {number} longitude
 * @returns {Promise<string>} City/town name
 */
export const reverseGeocode = async (latitude, longitude) => {
  // Validate coordinates
  if (typeof latitude !== 'number' || typeof longitude !== 'number') {
    throw new Error('Invalid coordinates.');
  }

  // Check cache first
  const cacheKey = `${roundCoord(latitude)}_${roundCoord(longitude)}`;
  if (cache.has(cacheKey)) {
    return cache.get(cacheKey);
  }

  const MAX_RETRIES = 3;
  let lastError;

  for (let attempt = 0; attempt < MAX_RETRIES; attempt++) {
    try {
      // Respect rate limit: wait before retry (exponential backoff)
      if (attempt > 0) {
        await new Promise((resolve) => setTimeout(resolve, 1000 * Math.pow(2, attempt)));
      }

      const response = await axios.get(BASE_URL, {
        params: { lat: latitude, lon: longitude },
        timeout: 10000,
      });

      const city = extractCityName(response.data?.address);
      if (!city) {
        throw new Error('Could not determine city from coordinates.');
      }

      // Cache the result
      cache.set(cacheKey, city);
      return city;
    } catch (error) {
      lastError = error;
      console.error(`Reverse geocoding attempt ${attempt + 1} failed:`, error.message);
    }
  }

  throw new Error(
    lastError?.message || 'Reverse geocoding failed after multiple attempts.'
  );
};

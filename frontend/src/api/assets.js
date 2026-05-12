import client from "./client";

/**
 * Asset and price-data API.
 */

/** @returns {Promise<import("axios").AxiosResponse>} list of AssetResponse */
export const listAssets = () => client.get("/assets");

/**
 * @param {number} id - asset ID
 * @param {string} range - TimeRange enum value (e.g. "ONE_YEAR")
 * @returns {Promise<import("axios").AxiosResponse>} list of PricePointResponse
 */
export const getPrices = (id, range = "ONE_YEAR") =>
  client.get(`/assets/${id}/prices`, { params: { range } });

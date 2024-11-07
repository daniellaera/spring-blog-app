const parseApiUrl = (url: string) => {
  // In this case, we expect URL to be a string, but you could modify for other types if needed
  return url;
};

export const environment = {
  production: true,
  apiUrl: parseApiUrl('${API_URL}')
};

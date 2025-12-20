// Minimal axios-like wrapper using fetch as a fallback
// Only defines if window.axios is not already available
(function() {
  if (window.axios) return;

  function isFormData(val) {
    return typeof FormData !== 'undefined' && val instanceof FormData;
  }

  function buildQuery(params) {
    if (!params) return '';
    const usp = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value === undefined || value === null) return;
      if (Array.isArray(value)) {
        value.forEach(v => usp.append(key, v));
      } else {
        usp.append(key, value);
      }
    });
    const qs = usp.toString();
    return qs ? (urlHasQuery ? '&' + qs : '?' + qs) : '';
  }

  async function request(method, url, config = {}) {
    const { params, headers, data } = config;
    let finalUrl = url;
    if (params && typeof params === 'object') {
      const usp = new URLSearchParams();
      Object.entries(params).forEach(([k, v]) => {
        if (v === undefined || v === null) return;
        if (Array.isArray(v)) {
          v.forEach(x => usp.append(k, x));
        } else {
          usp.append(k, v);
        }
      });
      const q = usp.toString();
      if (q) {
        finalUrl += (finalUrl.includes('?') ? '&' : '?') + q;
      }
    }

    const init = { method, headers: { ...(headers || {}) } };

    if (method !== 'GET' && method !== 'HEAD') {
      if (isFormData(data)) {
        init.body = data;
        // Let browser set Content-Type with proper boundary
      } else if (data !== undefined) {
        init.body = typeof data === 'string' ? data : JSON.stringify(data);
        if (!init.headers['Content-Type'] && typeof data !== 'string') {
          init.headers['Content-Type'] = 'application/json';
        }
      }
    }

    const res = await fetch(finalUrl, init);
    const contentType = res.headers.get('content-type') || '';
    let parsed;
    try {
      if (contentType.includes('application/json')) {
        parsed = await res.json();
      } else {
        parsed = await res.text();
      }
    } catch (e) {
      parsed = null;
    }

    if (!res.ok) {
      const err = new Error('Request failed with status ' + res.status);
      err.response = { status: res.status, data: parsed, headers: res.headers };
      throw err;
    }

    return { data: parsed, status: res.status, headers: res.headers };
  }

  const axiosLite = {
    get: (url, config = {}) => request('GET', url, config),
    post: (url, data, config = {}) => request('POST', url, { ...config, data }),
    put: (url, data, config = {}) => request('PUT', url, { ...config, data }),
    delete: (url, config = {}) => request('DELETE', url, config),
  };

  window.axios = axiosLite;
})();
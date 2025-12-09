// header.js  (place at /admin/header.js)
(function () {
  const headerContainerId = 'shared-header';
  const headerUrl = 'header.html';

  async function loadHeader() {
    const container = document.getElementById(headerContainerId);
    if (!container) return;
    try {
      const resp = await fetch(headerUrl, { cache: 'no-store' });
      if (!resp.ok) {
        container.innerHTML = '<div style="padding:12px;color:var(--muted)">Header failed to load</div>';
        return;
      }
      container.innerHTML = await resp.text();
      wireTheme();
      wireNavActive();
      wireSearch();
      window._sharedHeaderLoaded = true;
    } catch (e) {
      console.error('Failed loading header:', e);
      container.innerHTML = '<div style="padding:12px;color:var(--muted)">Header failed to load</div>';
    }
  }

  function applyTheme(theme) {
    if (theme === 'light') document.body.classList.add('light'); else document.body.classList.remove('light');
    const label = document.getElementById('themeLabel');
    const icon = document.getElementById('themeIcon');
    if (label) label.textContent = theme === 'light' ? 'Light' : 'Dark';
    if (icon) icon.textContent = theme === 'light' ? '☀️' : '🌙';
    localStorage.setItem('dashboardTheme', theme);
  }

  function toggleTheme() {
    const cur = document.body.classList.contains('light') ? 'light' : 'dark';
    applyTheme(cur === 'light' ? 'dark' : 'light');
  }

  function wireTheme() {
    const saved = localStorage.getItem('dashboardTheme');
    if (saved) applyTheme(saved);
    else {
      const prefersLight = window.matchMedia && window.matchMedia('(prefers-color-scheme: light)').matches;
      applyTheme(prefersLight ? 'light' : 'dark');
    }
    const btn = document.getElementById('globalThemeBtn');
    if (btn && !btn._bound) {
      btn.addEventListener('click', toggleTheme);
      btn._bound = true;
    }
    window.addEventListener('storage', (ev)=> {
      if (ev.key === 'dashboardTheme' && ev.newValue) applyTheme(ev.newValue);
    });
  }

  function wireNavActive() {
    const links = document.querySelectorAll('#' + headerContainerId + ' a[href]');
    const path = (location.pathname || '').split('/').pop() || 'dashboard.html';
    links.forEach(a => {
      a.style.opacity = '0.85';
      a.style.textDecoration = 'none';
      if (a.getAttribute('href') === path || (path === '' && a.getAttribute('href') === 'dashboard.html')) {
        a.style.fontWeight = '900';
        a.style.opacity = '1';
        a.style.textDecoration = 'underline';
      }
    });
  }

  function wireSearch() {
    const input = document.getElementById('globalSearch');
    const btn = document.getElementById('searchBtn');
    if (!input || !btn) return;
    btn.addEventListener('click', ()=> {
      const q = input.value.trim();
      if (!q) return;
      // default to staff search; you can customize
      location.href = `staff.html?q=${encodeURIComponent(q)}`;
    });
    input.addEventListener('keydown', (e)=> {
      if (e.key === 'Enter') btn.click();
    });
  }

  window.SharedHeader = { applyTheme, toggleTheme, loadHeader };

  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', loadHeader);
  else loadHeader();
})();

import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import './i18n';
import './theme/accessibility.css';
import './theme/responsive.css';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
);

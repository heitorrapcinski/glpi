import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BrowserRouter } from 'react-router-dom';
import AppRoutes from './router';
import OfflineIndicator from './components/pwa/OfflineIndicator';
import ServiceWorkerUpdateBanner from './components/pwa/ServiceWorkerUpdateBanner';
import { ToastProvider } from './components/common/Toast';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 5 * 60 * 1000,
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <ToastProvider>
          <OfflineIndicator />
          <div id="glpi-app">
            <AppRoutes />
          </div>
          <ServiceWorkerUpdateBanner />
        </ToastProvider>
      </BrowserRouter>
    </QueryClientProvider>
  );
}

export default App;

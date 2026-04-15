import PageMeta from "../../components/common/PageMeta";
import { useAuth } from "../../context/AuthContext";

export default function Home() {
  const { isAuthenticated } = useAuth();

  return (
    <>
      <PageMeta
        title="Dashboard | GLPI"
        description="GLPI IT Service Management Dashboard"
      />
      <div className="grid grid-cols-12 gap-4 md:gap-6">
        {/* Welcome banner */}
        <div className="col-span-12">
          <div className="rounded-2xl border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-white/[0.03]">
            <h2 className="text-xl font-semibold text-gray-800 dark:text-white/90">
              {isAuthenticated ? "Welcome to GLPI" : "Loading…"}
            </h2>
            <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
              IT Service Management Dashboard
            </p>
          </div>
        </div>

        {/* Open Tickets */}
        <div className="col-span-12 sm:col-span-6 xl:col-span-3">
          <div className="rounded-2xl border border-gray-200 bg-white p-5 dark:border-gray-800 dark:bg-white/[0.03]">
            <div className="flex items-center gap-4">
              <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-brand-50 dark:bg-brand-500/10">
                <svg className="h-6 w-6 text-brand-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M16.5 6v.75m0 3v.75m0 3v.75m0 3V18m-9-5.25h5.25M7.5 15h3M3.375 5.25c-.621 0-1.125.504-1.125 1.125v3.026a2.999 2.999 0 0 1 0 5.198v3.026c0 .621.504 1.125 1.125 1.125h17.25c.621 0 1.125-.504 1.125-1.125v-3.026a2.999 2.999 0 0 1 0-5.198V6.375c0-.621-.504-1.125-1.125-1.125H3.375Z" />
                </svg>
              </div>
              <div>
                <span className="text-sm text-gray-500 dark:text-gray-400">Open Tickets</span>
                <h4 className="mt-1 text-2xl font-bold text-gray-800 dark:text-white/90">—</h4>
              </div>
            </div>
          </div>
        </div>

        {/* Pending Requests */}
        <div className="col-span-12 sm:col-span-6 xl:col-span-3">
          <div className="rounded-2xl border border-gray-200 bg-white p-5 dark:border-gray-800 dark:bg-white/[0.03]">
            <div className="flex items-center gap-4">
              <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-amber-50 dark:bg-amber-500/10">
                <svg className="h-6 w-6 text-amber-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M12 6v6h4.5m4.5 0a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z" />
                </svg>
              </div>
              <div>
                <span className="text-sm text-gray-500 dark:text-gray-400">Pending Requests</span>
                <h4 className="mt-1 text-2xl font-bold text-gray-800 dark:text-white/90">—</h4>
              </div>
            </div>
          </div>
        </div>

        {/* Active Problems */}
        <div className="col-span-12 sm:col-span-6 xl:col-span-3">
          <div className="rounded-2xl border border-gray-200 bg-white p-5 dark:border-gray-800 dark:bg-white/[0.03]">
            <div className="flex items-center gap-4">
              <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-red-50 dark:bg-red-500/10">
                <svg className="h-6 w-6 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v3.75m-9.303 3.376c-.866 1.5.217 3.374 1.948 3.374h14.71c1.73 0 2.813-1.874 1.948-3.374L13.949 3.378c-.866-1.5-3.032-1.5-3.898 0L2.697 16.126ZM12 15.75h.007v.008H12v-.008Z" />
                </svg>
              </div>
              <div>
                <span className="text-sm text-gray-500 dark:text-gray-400">Active Problems</span>
                <h4 className="mt-1 text-2xl font-bold text-gray-800 dark:text-white/90">—</h4>
              </div>
            </div>
          </div>
        </div>

        {/* Scheduled Changes */}
        <div className="col-span-12 sm:col-span-6 xl:col-span-3">
          <div className="rounded-2xl border border-gray-200 bg-white p-5 dark:border-gray-800 dark:bg-white/[0.03]">
            <div className="flex items-center gap-4">
              <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-emerald-50 dark:bg-emerald-500/10">
                <svg className="h-6 w-6 text-emerald-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M19.5 12c0-1.232-.046-2.453-.138-3.662a4.006 4.006 0 0 0-3.7-3.7 48.678 48.678 0 0 0-7.324 0 4.006 4.006 0 0 0-3.7 3.7c-.017.22-.032.441-.046.662M19.5 12l3-3m-3 3-3-3m-12 3c0 1.232.046 2.453.138 3.662a4.006 4.006 0 0 0 3.7 3.7 48.656 48.656 0 0 0 7.324 0 4.006 4.006 0 0 0 3.7-3.7c.017-.22.032-.441.046-.662M4.5 12l3 3m-3-3-3 3" />
                </svg>
              </div>
              <div>
                <span className="text-sm text-gray-500 dark:text-gray-400">Scheduled Changes</span>
                <h4 className="mt-1 text-2xl font-bold text-gray-800 dark:text-white/90">—</h4>
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}

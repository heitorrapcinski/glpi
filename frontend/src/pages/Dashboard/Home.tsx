import PageMeta from "../../components/common/PageMeta";

export default function Home() {
  return (
    <>
      <PageMeta
        title="Dashboard | GLPI"
        description="GLPI IT Service Management Dashboard"
      />
      <div className="grid grid-cols-12 gap-4 md:gap-6">
        <div className="col-span-12">
          <div className="rounded-2xl border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-white/[0.03]">
            <h2 className="text-xl font-semibold text-gray-800 dark:text-white/90">
              Welcome to GLPI
            </h2>
            <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
              IT Service Management Dashboard
            </p>
          </div>
        </div>

        <div className="col-span-12 sm:col-span-6 xl:col-span-3">
          <div className="rounded-2xl border border-gray-200 bg-white p-5 dark:border-gray-800 dark:bg-white/[0.03]">
            <div className="flex items-center justify-between">
              <div>
                <span className="text-sm text-gray-500 dark:text-gray-400">Open Tickets</span>
                <h4 className="mt-2 text-2xl font-bold text-gray-800 dark:text-white/90">—</h4>
              </div>
            </div>
          </div>
        </div>

        <div className="col-span-12 sm:col-span-6 xl:col-span-3">
          <div className="rounded-2xl border border-gray-200 bg-white p-5 dark:border-gray-800 dark:bg-white/[0.03]">
            <div className="flex items-center justify-between">
              <div>
                <span className="text-sm text-gray-500 dark:text-gray-400">Pending Requests</span>
                <h4 className="mt-2 text-2xl font-bold text-gray-800 dark:text-white/90">—</h4>
              </div>
            </div>
          </div>
        </div>

        <div className="col-span-12 sm:col-span-6 xl:col-span-3">
          <div className="rounded-2xl border border-gray-200 bg-white p-5 dark:border-gray-800 dark:bg-white/[0.03]">
            <div className="flex items-center justify-between">
              <div>
                <span className="text-sm text-gray-500 dark:text-gray-400">Active Changes</span>
                <h4 className="mt-2 text-2xl font-bold text-gray-800 dark:text-white/90">—</h4>
              </div>
            </div>
          </div>
        </div>

        <div className="col-span-12 sm:col-span-6 xl:col-span-3">
          <div className="rounded-2xl border border-gray-200 bg-white p-5 dark:border-gray-800 dark:bg-white/[0.03]">
            <div className="flex items-center justify-between">
              <div>
                <span className="text-sm text-gray-500 dark:text-gray-400">Assets</span>
                <h4 className="mt-2 text-2xl font-bold text-gray-800 dark:text-white/90">—</h4>
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}

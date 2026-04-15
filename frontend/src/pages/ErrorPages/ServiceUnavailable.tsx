import GridShape from "../../components/common/GridShape";
import { Link } from "react-router";
import PageMeta from "../../components/common/PageMeta";

export default function ServiceUnavailable() {
  return (
    <>
      <PageMeta
        title="503 - Service Unavailable | GLPI"
        description="The service is temporarily unavailable. Please try again in a few moments."
      />
      <div className="relative flex flex-col items-center justify-center min-h-screen p-6 overflow-hidden z-1">
        <GridShape />
        <div className="mx-auto w-full max-w-[242px] text-center sm:max-w-[472px]">
          <h1 className="mb-8 font-bold text-gray-800 text-title-md dark:text-white/90 xl:text-title-2xl">
            Service Unavailable
          </h1>

          <img
            src="/images/error/503.svg"
            alt="503 - Service Unavailable"
            className="dark:hidden"
          />
          <img
            src="/images/error/503-dark.svg"
            alt="503 - Service Unavailable"
            className="hidden dark:block"
          />

          <p className="mt-10 mb-6 text-base text-gray-700 dark:text-gray-400 sm:text-lg">
            The service is temporarily unavailable. Please try again in a few
            moments.
          </p>

          <div className="flex flex-col items-center gap-3 sm:flex-row sm:justify-center">
            <button
              onClick={() => window.location.reload()}
              className="inline-flex items-center justify-center rounded-lg bg-brand-500 px-5 py-3.5 text-sm font-medium text-white shadow-theme-xs hover:bg-brand-600"
            >
              Try Again
            </button>
            <Link
              to="/"
              className="inline-flex items-center justify-center rounded-lg border border-gray-300 bg-white px-5 py-3.5 text-sm font-medium text-gray-700 shadow-theme-xs hover:bg-gray-50 hover:text-gray-800 dark:border-gray-700 dark:bg-gray-800 dark:text-gray-400 dark:hover:bg-white/[0.03] dark:hover:text-gray-200"
            >
              Back to Dashboard
            </Link>
          </div>
        </div>
      </div>
    </>
  );
}

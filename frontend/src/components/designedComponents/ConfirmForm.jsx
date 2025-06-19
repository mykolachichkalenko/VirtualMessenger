export default function ConfirmForm({onClose,text,title,onConfirm}){
    return (
        <div
            className="fixed inset-0 z-50 grid place-content-center bg-black/50 p-4"
            role="dialog"
            aria-modal="true"
            aria-labelledby="modalTitle"
        >
            <div className="w-full max-w-md rounded-lg bg-white p-6 shadow-lg dark:bg-gray-900">
                <div className="flex items-start justify-between">
                    <h2 id="modalTitle" className="text-xl font-bold text-gray-900 sm:text-2xl dark:text-white">
                        {title}
                    </h2>

                    <button
                        type="button"
                        className="-me-4 -mt-4 rounded-full p-2 text-gray-400 transition-colors hover:bg-gray-50 hover:text-gray-600 focus:outline-none dark:text-gray-500 dark:hover:bg-gray-800 dark:hover:text-gray-300"
                        aria-label="Close"
                        onClick={onClose}
                    >
                        <svg
                            xmlns="http://www.w3.org/2000/svg"
                            className="size-5"
                            fill="none"
                            viewBox="0 0 24 24"
                            stroke="currentColor"
                        >
                            <path
                                strokeLinecap="round"
                                strokeLinejoin="round"
                                strokeWidth="2"
                                d="M6 18L18 6M6 6l12 12"
                            />
                        </svg>
                    </button>
                </div>

                <div className="mt-4">
                    <p className="text-pretty text-gray-700 dark:text-gray-200">
                        {text}
                    </p>
                </div>

                <footer className="mt-6 flex justify-end gap-2">
                    <button
                        type="button"
                        className="rounded bg-red-500 px-4 py-2 text-sm font-medium text-gray-700 transition-colors hover:bg-red-600 dark:bg-red-500 dark:text-gray-200 dark:hover:bg-red-700"
                        onClick={onClose}
                    >
                        Cancel
                    </button>

                    <button
                        type="button"
                        className="rounded bg-green-600 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-green-700"
                        onClick={onConfirm}
                    >
                        Confirm
                    </button>
                </footer>
            </div>
        </div>
    )
}

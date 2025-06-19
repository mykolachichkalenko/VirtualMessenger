import {useEffect, useRef, useState} from "react";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faEllipsis, faPen, faTrash} from "@fortawesome/free-solid-svg-icons";

function useOutsideClick(ref, handler) {
    useEffect(() => {
        const listener = (event) => {
            if (!ref.current || ref.current.contains(event.target)) return;
            handler();
        };
        document.addEventListener("mousedown", listener);
        return () => document.removeEventListener("mousedown", listener);
    }, [ref, handler]);
}

export function VerticalDropdown({message, webSocket,messages,setMessages,setMessageForEdit}) {
    const [isOpen, setIsOpen] = useState(false);
    const ref = useRef(null);

    useOutsideClick(ref, () => setIsOpen(false));
    //deleting message through webSocket
    const deleteMessage = () => {
        if (webSocket.current?.readyState === WebSocket.OPEN) {
            setIsOpen(false);

            const updatedMessages = messages.filter(msg => msg.id !== message.id);
            setMessages(updatedMessages)

            const correctMessage = {
                id:message.id,
                chatId:message.chatId,
                senderPhone:message.senderPhone,
                receiverPhone:message.receiverPhone,
                content:message.content,
                type:message.type,
                sentAt:"2025-06-09T21:42:00",
                isRead:message.read
            }
            webSocket.current.send(JSON.stringify(correctMessage));
        }
    }

    return (
        <div className="relative inline-block" ref={ref}>
            <button
                id="dropdownMenuIconButton"
                onClick={() => setIsOpen(v => !v)}
                className="inline-flex items-center text-sm font-medium text-gray-900 bg-white rounded-lg hover:bg-gray-100 focus:ring-4 focus:outline-none dark:text-white focus:ring-gray-50 dark:bg-gray-800 dark:hover:bg-gray-700 dark:focus:ring-gray-600"
                type="button"
                aria-expanded={isOpen}
                aria-controls="dropdownDots"
            >
                <FontAwesomeIcon icon={faEllipsis}/>
            </button>

            {isOpen && (
                <div
                    id="dropdownDots"
                    className="z-10 absolute top-1/2 right-full mr-2 w-44 bg-white divide-y divide-gray-100 rounded-lg shadow-sm dark:bg-gray-700 dark:divide-gray-600 -translate-y-1/2"
                >
                    <ul
                        className="py-2 text-sm text-gray-700 dark:text-gray-200"
                        aria-labelledby="dropdownMenuIconButton"
                    >
                        {message.type === "TEXT" && <li>
                            <a
                                href="#"
                                className="block px-4 py-2 rounded text-[#0d0d0d] hover:bg-gray-100 dark:hover:bg-gray-600 dark:hover:text-white"
                                onClick={() => setMessageForEdit(message)}>
                                <FontAwesomeIcon icon={faPen} size={"1x"} className={"mr-1"}/> Edit
                            </a>
                        </li>}
                        <li>
                            <a
                                href="#"
                                className="block px-4 py-2 rounded text-[#0d0d0d] hover:bg-red-600 dark:hover:bg-red-600 dark:hover:text-white"
                                onClick={deleteMessage}
                            >
                                <FontAwesomeIcon icon={faTrash} className={"mr-1"}/> Delete
                            </a>
                        </li>
                    </ul>
                </div>
            )}
        </div>

    );
}
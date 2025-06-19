import {useEffect, useRef, useState} from "react";
import {EventSourcePolyfill} from "event-source-polyfill";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faTrash} from "@fortawesome/free-solid-svg-icons";
import {Chip} from "@heroui/react";

export default function Chats({myPhone}) {
    const [chats, setChats] = useState([]);
    const socket = useRef(null);
    //getting all my chats
    useEffect(() => {
        if (!myPhone) return;

        const sse = new EventSourcePolyfill("http://localhost:8080/chat/get/my/all",{withCredentials:true});

        sse.onmessage=(event)=>{
            if (!event.data) return;
            const incomingChat = JSON.parse(event.data);

            setChats(prevChats => {
                const existingIndex = prevChats.findIndex(chat => chat.id === incomingChat.id);
                let updatedChats;

                if (existingIndex !== -1) {
                    updatedChats = [...prevChats];
                    updatedChats[existingIndex] = {
                        ...updatedChats[existingIndex],
                        last_updated: incomingChat.last_updated,
                        unread_for_first_user: incomingChat.unread_for_first_user,
                        unread_for_second_user: incomingChat.unread_for_second_user
                    };
                } else {
                    updatedChats = [{...incomingChat,last_updated:
                            new Date(
                                incomingChat.last_updated[0],
                                incomingChat.last_updated[1]-1,
                                incomingChat.last_updated[2],
                                incomingChat.last_updated[3],
                                incomingChat.last_updated[4],
                                incomingChat.last_updated[5]
                            )}, ...prevChats];
                }

                return updatedChats.sort((a, b) => new Date(b.last_updated) - new Date(a.last_updated));
            });
        }

        return () => sse.close();
    }, [myPhone]);
    //deleting chat
    const deleteChat = (id) => {
        if (socket.current){
            socket.current.send(JSON.stringify(id));
        }
        setChats(prevChats =>
            prevChats
                .filter(chat => chat.id !== id)
                .sort((a, b) => new Date(b.last_updated) - new Date(a.last_updated))
        );
    }
    //websocketForDeleting
    useEffect(() => {
        if (!myPhone) return;

        const websocket = new WebSocket(`ws://localhost:8080/ws/chat/delete?phone=${myPhone}`);
        socket.current = websocket;

        websocket.onmessage= (event) =>{
            const id = JSON.parse(event.data);
            setChats(prevChats =>
                prevChats
                    .filter(chat => chat.id !== id)
                    .sort((a, b) => new Date(b.last_updated) - new Date(a.last_updated))
            );
        }

        return () => websocket.close();
    }, [myPhone]);
    return (
        <div >
            {chats.map(chat => (
                <div key={chat.id} className={`group flex items-center gap-5 p-4 hover:bg-black rounded-xl`}>
                    <img
                        src="https://img.freepik.com/free-photo/smooth-gray-background-with-high-quality_53876-124606.jpg?semt=ais_hybrid&w=740"
                        alt="avatar"
                        className="w-20 h-20 rounded-full cursor-pointer"
                        onClick={() => window.location.href = `/${chat.first_user_phone === myPhone.toString()
                            ? chat.second_user_phone
                            : chat.first_user_phone}`}
                    />
                    <div>
                        <p className="text-xl font-semibold">
                            {chat.first_user_phone === myPhone.toString()
                                ? chat.second_user_phone
                                : chat.first_user_phone}
                        </p>
                        {chat.first_user_phone === myPhone.toString()
                            ? chat.unread_for_first_user && (
                            <Chip color={"primary"} size={"sm"}>NEW</Chip>
                        )
                            : chat.unread_for_second_user && (
                            <Chip color={"primary"} size={"sm"}>NEW</Chip>
                        )}
                    </div>
                    <div className={"hidden group-hover:block"}>
                        <FontAwesomeIcon icon={faTrash} onClick={() => deleteChat(chat.id)}
                                         className={"cursor-pointer hover:text-red-600"}/>
                    </div>
                </div>
            ))}
        </div>
    )
}
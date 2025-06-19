import {useEffect, useRef, useState} from "react";
import axios from "axios";
import {faCheck, faCheckDouble} from "@fortawesome/free-solid-svg-icons";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {EventSourcePolyfill} from "event-source-polyfill";
import {VerticalDropdown} from "./VerticalDropdown.jsx";

export default function Messages({
                                     phone,
                                     chat,
                                     socketMessage,
                                     messageSent,
                                     myPhone,
                                     setMessageForEditP,
                                     socketForEditedMessage,
                                     updatedMessage,
                                     setUpdatedMessage,
                                     socketReload
                                 }) {
    const [messages, setMessages] = useState([]);
    const [page, setPage] = useState(0);
    const messagesEndRef = useRef(null);
    const containerRef = useRef(null);
    const socketForDeletingMessageRef = useRef(null);
    const [messageForEdit, setMessageForEdit] = useState(null);
    //logic of scrolling to bottom
    const scrollToBottom = () => {
        setTimeout(() => {
            messagesEndRef.current?.scrollIntoView({behavior: 'smooth'});
        }, 100);
    };
    useEffect(() => {
        scrollToBottom();
    }, []);
    //function for setting marked
    const setMarkedById = (id) => {
        if (!Array.isArray(messages)) return;

        setMessages(prevMessages =>
            prevMessages.map(msg => {
                if (id === 0 || msg.id < id) {
                    return {...msg, read: true};
                }
                return msg;
            })
        );
    }
    //sse for checking last unread message
    useEffect(() => {
        if (!phone || !chat) return;

        const sse = new EventSourcePolyfill(`http://localhost:8080/messages/get/last/unred?chatId=${chat.id}&shard=${chat.shard_of_chat}&phoneUser=${phone}`, {
            withCredentials: true
        })

        sse.onmessage = (event) => {
            if (event.data) {
                const id = JSON.parse(event.data);
                setMarkedById(id);
            } else {
                console.log("null id");
            }
        }

    }, [chat, phone]);
    //checking websocket new messages and correcting it
    useEffect(() => {
        if (!socketMessage.current) return;

        socketMessage.current.onmessage = (e) => {
            const checkOnly = JSON.parse(e.data);
            if (checkOnly.senderPhone === phone) {
                scrollToBottom()
                if (chat && phone) {
                    axios.post("http://localhost:8080/messages/mark-as-red", {
                        shard: chat.shard_of_chat.toString(),
                        chatId: chat.id.toString(),
                        phoneUser: phone
                    }, {withCredentials: true}).then();
                }
                try {
                    const msg = JSON.parse(e.data);
                    setMessages(prevState => {
                        const maxId = prevState.length > 0
                            ? prevState.reduce((max, msg) => msg.id > max ? msg.id : max, prevState[0].id) : 0;

                        const newMessage = {
                            ...msg,
                            id: maxId + 1,
                            sentAt: new Date(msg.sentAt).toLocaleTimeString([], {hour: '2-digit', minute: '2-digit'}),
                            read: true
                        };
                        return [...prevState, newMessage]
                    })
                } catch (err) {
                    console.error("error:", e.data);
                }
            }else{
                axios.post("http://localhost:8080/chat/set/unread",{receiverPhone:myPhone, senderPhone:checkOnly.senderPhone}
                    ,{withCredentials:true}).then();
            }
        };
    }, [socketMessage.current]);
    //getting conservation history
    useEffect(() => {
        if (!chat) return;

        const container = containerRef.current;
        const prevScrollHeight = container?.scrollHeight ?? 0;

        axios.get("http://localhost:8080/messages/get", {
            withCredentials: true,
            params: {
                chatId: chat.id.toString(),
                page: page.toString(),
                shard: chat.shard_of_chat.toString()
            }
        }).then(resp => {
            const sortedMessages = resp.data
                .map(msg => ({
                    ...msg,
                    sentAt: new Date(
                        msg.sentAt[0],
                        msg.sentAt[1] - 1,
                        msg.sentAt[2],
                        msg.sentAt[3],
                        msg.sentAt[4],
                        msg.sentAt[5]
                    )
                }))
                .sort((a, b) => (a.id || 0) - (b.id || 0));

            setMessages(prevState => {
                const updated = [...sortedMessages, ...prevState];
                setTimeout(() => {
                    const newScrollHeight = container?.scrollHeight ?? 0;
                    const scrollDiff = newScrollHeight - prevScrollHeight;

                    if (container) {
                        container.scrollTop = scrollDiff;
                    }
                }, 0);

                return updated;
            });
        });
    }, [chat, page]);
    //checking message that i sent through websocket and correcting it
    useEffect(() => {
        if (!messageSent) return;
        setMessages(prevState => {
                const maxId = prevState.length > 0
                    ? prevState.reduce((max, msg) => msg.id > max ? msg.id : max, prevState[0].id) : 0;

                const newMessage = {
                    ...messageSent,
                    id: maxId + 1,
                    chatId: chat.chatId
                };
                return [...prevState, newMessage];
            }
        )
        scrollToBottom();
    }, [messageSent]);
    //history adding while scrolling up
    useEffect(() => {
        const container = containerRef.current;
        if (!container) return;

        const handleScroll = () => {
            if (container.scrollTop === 0) {
                setPage(prev => prev + 1);
            }
        };
        container.addEventListener("scroll", handleScroll);
        return () => container.removeEventListener("scroll", handleScroll);
    }, []);
    //websocket deleting message
    useEffect(() => {
        if (!myPhone) return;

        const socket = new WebSocket(`ws://localhost:8080/ws/chat/message/delete?phone=${myPhone}`);
        socketForDeletingMessageRef.current = socket;

        if (socketForDeletingMessageRef.current) {
            socketForDeletingMessageRef.current.onmessage = (event) => {
                const message = JSON.parse(event.data);
                setMessages(prevMessages => deleteMessageSmart(prevMessages, message));
            }
        }
        return () => {
            console.log("websocket closed");
            socket.close();
        };
    }, [myPhone]);

    //function for deleting by id message that i received from webSocket
    function deleteMessageSmart(messages, target) {
        const exactIndex = messages.findIndex(msg =>
            msg.id === target.id &&
            msg.senderPhone === target.senderPhone &&
            msg.receiverPhone === target.receiverPhone
        );

        if (exactIndex !== -1) {
            return [
                ...messages.slice(0, exactIndex),
                ...messages.slice(exactIndex + 1)
            ];
        }

        const fallbackIndex = messages.findIndex(msg =>
            msg.id > target.id &&
            msg.senderPhone === target.senderPhone &&
            msg.receiverPhone === target.receiverPhone
        );

        if (fallbackIndex !== -1) {
            return [
                ...messages.slice(0, fallbackIndex),
                ...messages.slice(fallbackIndex + 1)
            ];
        }

        return messages;
    }

    //functional for message editing
    useEffect(() => {
        setMessageForEditP(messageForEdit);
    }, [messageForEdit]);
    //websocket receiving edited message
    useEffect(() => {
        if (socketForEditedMessage.current) {
            socketForEditedMessage.current.onmessage = (event) => {
                const editedMessageCorrect = JSON.parse(event.data);
                setMessages(prevState => editMessageSmart(prevState, editedMessageCorrect));
            }
        }
    }, [socketForEditedMessage.current]);

    //function for updation message
    function editMessageSmart(messages, target) {
        const {id, senderPhone, receiverPhone, content} = target;

        const exactIndex = messages.findIndex(msg =>
            msg.id === id &&
            msg.senderPhone === senderPhone &&
            msg.receiverPhone === receiverPhone
        );

        if (exactIndex !== -1) {
            const updatedMessages = [...messages];
            updatedMessages[exactIndex] = {...updatedMessages[exactIndex], content};
            return updatedMessages;
        }

        const fallbackIndex = messages.findIndex(msg =>
            msg.id > id &&
            msg.senderPhone === senderPhone &&
            msg.receiverPhone === receiverPhone
        );

        if (fallbackIndex !== -1) {
            const updatedMessages = [...messages];
            updatedMessages[fallbackIndex] = {...updatedMessages[fallbackIndex], content};
            return updatedMessages;
        }

        return messages;
    }

    //setting edited message
    function editMessageById(messages, updatedMessage) {
        return messages.map(msg =>
            msg.id === updatedMessage.id
                ? {...msg, content: updatedMessage.content}
                : msg
        );
    }

    useEffect(() => {
        if (!updatedMessage) return;

        setMessages(prevState => editMessageById(prevState, updatedMessage));

        setUpdatedMessage(null);
    }, [updatedMessage]);
    return (
        <div className={"w-[50%] max-2xl:w-[90%] h-[88%] overflow-y-auto px-4 py-2 flex flex-col gap-2 scrollbar-hide"}
             ref={containerRef}>
            {messages.map(msg => (
                <div key={msg.id} className={`relative group flex-col gap-1 break-words break-all max-w-[50%] min-w-[10%] flex px-2 py-1 rounded-xl my-1 text-3xl mb-2
                    ${msg.senderPhone === myPhone.toString() ? "bg-gray-800 self-end" : "bg-[#0d0d0d] self-start"} `}>
                    {msg.type === "TEXT" ?
                        <p className={"mb-1"}>{msg.content}</p>
                        : msg.type === "PHOTO" ?
                        <img src={msg.content} alt={"photo"}/>
                            :
                            <video src={msg.content} controls/>
                    }
                    <div className={`text-sm self-end text-gray-500 flex `}>

                        {msg.senderPhone !== phone &&
                            <div
                                className={"hidden group-hover:block transition-opacity duration-150 relative right-2 top-0.5"}>
                                <VerticalDropdown message={msg} webSocket={socketForDeletingMessageRef}
                                                  messages={messages} setMessages={setMessages}
                                                  setMessageForEdit={setMessageForEdit}>
                                </VerticalDropdown>
                            </div>}

                        {msg.sentAt.toLocaleString([], {
                            hour: '2-digit',
                            minute: '2-digit'
                        })}

                        {msg.senderPhone === myPhone.toString() ? msg.read ?
                            <FontAwesomeIcon icon={faCheckDouble} className={"ml-1 mt-1"}/>
                            : <FontAwesomeIcon icon={faCheck} className={"ml-1 mt-1"}/> : <></>}
                    </div>
                </div>

            ))}
            <div ref={messagesEndRef}>
            </div>
        </div>
    )
}

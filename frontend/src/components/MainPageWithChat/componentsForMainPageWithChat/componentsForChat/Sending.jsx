import {
    Autocomplete,
    AutocompleteItem,
    Button,
    Dropdown,
    DropdownItem,
    DropdownMenu,
    DropdownTrigger,
    Input
} from "@heroui/react";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faChevronUp, faImages, faPaperPlane, faPen, faXmark} from "@fortawesome/free-solid-svg-icons";
import {useEffect, useRef, useState} from "react";
import axios from "axios";

export default function Sending({
                                    onMessageChange,
                                    chat,
                                    sendingSocket,
                                    myPhone,
                                    receiverPhone,
                                    setOnSend,
                                    setMessageForEdit,
                                    messageForEdit,
                                    socketEditing,
                                    setEditedAndCorrectMessage,
                                    reloadSocket
                                }) {
    const [messageText, setMessageText] = useState("");
    const [isMessageTextCorrect, setIsMessageTextCorrect] = useState(false);
    const [editedMessage, setEditedMessage] = useState("");
    const [inEditingProcess, setInEditingProcess] = useState(false);
    const [isEditedMessageCorrect, setIsEditedMessageCorrect] = useState(false);
    const photoRef = useRef(null);
    const [photo, setPhoto] = useState(null);
    const [temporaryURLPhoto, setTemporaryURLPhoto] = useState("");
    const videoRef = useRef(null);
    const [video, setVideo] = useState(null);
    const [temporaryURLVideo, setTemporaryURLVideo] = useState("");
    const [language, setLanguage] = useState("");
    const [isCorrectMessageToCorrectIt, setIsCorrectMessageToCorrectIt] = useState(false);
    const [sending, setSending] = useState(false);

    const messageChange = (e) => {
        setMessageText(e.target.value);
    }
    //sending message through websocket
    const sendMessageText = (e) => {
        if (isMessageTextCorrect && sendingSocket.current?.readyState === WebSocket.OPEN) {
            console.log("here");
            const message = {
                id: 0,
                chatId: 0,
                senderPhone: myPhone.toString(),
                receiverPhone: receiverPhone,
                content: messageText,
                type: "TEXT",
                sentAt: "2025-06-09T21:42:00",
                isRead: false
            }
            sendingSocket.current.send(JSON.stringify(message));
        }
        const message = {
            id: 0,
            chatId: chat.id,
            senderPhone: myPhone.toString(),
            receiverPhone: receiverPhone,
            content: messageText,
            type: "TEXT",
            sentAt: new Date(Date.now()).toLocaleTimeString([], {hour: '2-digit', minute: '2-digit'}),
            isRead: false
        }
        setOnSend(message);
        setMessageText("");
    }
    // checking correction of messageText and setting onMessageChange for websocket
    useEffect(() => {
        if (messageText) {
            onMessageChange(messageText);
            if (messageText.trim().length > 0 && messageText.length > 0) {
                setIsMessageTextCorrect(true);
            } else {
                setIsMessageTextCorrect(false);
            }
        } else {
            setIsMessageTextCorrect(false);
        }
    }, [messageText]);
    //editing message
    useEffect(() => {
        if (messageForEdit) {
            setInEditingProcess(true);
            setEditedMessage(messageForEdit.content)
        } else {
            setInEditingProcess(false)
            setEditedMessage("");
        }
    }, [messageForEdit]);

    const messageEditChange = (event) => {
        setEditedMessage(event.target.value);
    }
    useEffect(() => {
        if (editedMessage.length > 0 && editedMessage !== messageForEdit.content) {
            setIsEditedMessageCorrect(true);
        } else {
            setIsEditedMessageCorrect(false);
        }
    }, [editedMessage]);
    //escape the edit
    const escapeEdit = () => {
        setEditedMessage("");
        setIsEditedMessageCorrect(false)
        setInEditingProcess(false)
        setMessageForEdit(null);
    }
    //websocket send edited message
    const sendEditedMessage = () => {
        if (socketEditing.current?.readyState === WebSocket.OPEN) {
            const correctMessage = {
                id: messageForEdit.id,
                chatId: chat.id,
                senderPhone: myPhone.toString(),
                receiverPhone: receiverPhone,
                content: editedMessage,
                type: "TEXT",
                sentAt: "2025-06-09T21:42:00",
                isRead: false
            }
            setEditedAndCorrectMessage(correctMessage);
            console.log(correctMessage);
            socketEditing.current.send(JSON.stringify(correctMessage));
        }
        setMessageForEdit(null);
    }
    //photo functional
    const clickOnPhoto = () => {
        if (photoRef.current) {
            photoRef.current.click();
        }
    }

    const photoChange = (event) => {
        const file = event.target.files?.[0];
        event.target.value = null;
        closeAllForVideo();
        if (file) {
            setPhoto(file);
        } else {
            setPhoto(null);
            setTemporaryURLPhoto("");
        }
    }

    useEffect(() => {
        if (photo) {
            const url = URL.createObjectURL(photo);
            setTemporaryURLPhoto(url);

            return () => {
                setTemporaryURLPhoto("");
                URL.revokeObjectURL(url);
            };
        } else {
            setTemporaryURLPhoto("");
        }
    }, [photo]);

    const closeAllForPhoto = () => {
        setTemporaryURLPhoto("");
        setPhoto(null);
    }

    const sendingPhoto = () => {
        if (photo) {
            const formData = new FormData();
            formData.append("photo", photo);
            const chatId = chat.id.toString();
            setSending(true);

            axios.post(`http://localhost:8080/messages/add/photo/${myPhone.toString()}/${receiverPhone.toString()}/${chatId}`
                , formData,
                {
                    withCredentials: true,
                    headers: {
                        "Content-Type": "multipart/form-data"
                    }
                })
                .then(response => reload());
        }
        setPhoto(null);
        setTemporaryURLPhoto("");
    }
    const reload = () => {
        if (reloadSocket.current) {
            reloadSocket.current.send(JSON.stringify({receiverPhone}));
        }
        window.location.reload();
    }
    //video functional
    const clickOnVideo = () => {
        if (videoRef.current) {
            videoRef.current.click();
        }
    }

    const videoChange = (event) => {
        const file = event.target.files[0];
        event.target.value = null;
        closeAllForPhoto();
        if (file) {
            setVideo(file)
        } else {
            setVideo(null);
            setTemporaryURLVideo("");
        }
    }
    useEffect(() => {
        if (video) {
            const videoURL = URL.createObjectURL(video);
            setTemporaryURLVideo(videoURL);
            return () => {
                setTemporaryURLVideo("");
                URL.revokeObjectURL(videoURL);
            };
        } else {
            setTemporaryURLVideo("");
        }
    }, [video]);

    const closeAllForVideo = () => {
        setVideo(null);
        setTemporaryURLVideo("");
    }
    const sendingVideo = () => {
        if (video) {
            const formData = new FormData();
            formData.append("video", video);
            const chatId = chat.id.toString();
            setSending(true);

            axios.post(`http://localhost:8080/messages/add/video/${myPhone.toString()}/${receiverPhone.toString()}/${chatId}`
                , formData,
                {
                    withCredentials: true,
                    headers: {
                        "Content-Type": "multipart/form-data"
                    }
                })
                .then(response => reload());
        }
        setVideo(null);
        setTemporaryURLVideo("");
    }
    //functional for correcting and translating message
    useEffect(() => {
        if (messageText.length > 10 && language) {
            setIsCorrectMessageToCorrectIt(true);
        } else {
            setIsCorrectMessageToCorrectIt(false);
        }
    }, [language, messageText]);

    const correctMessage = async () => {
        if (isCorrectMessageToCorrectIt) {
            const me = await axios.get(`http://localhost:8080/get/user/${myPhone}`, {withCredentials: true});

            if (me.data.isPremium) {
                axios.post("http://localhost:8080/messages/correct/ai", {text: messageText, language: language},
                    {withCredentials: true})
                    .then(response => {
                        setMessageText(response.data.candidates[0].content.parts[0].text);
                    });
            } else {
                window.location.href = "/premium"
            }
        }
    }
    return (
        <div className={"flex h-auto w-full justify-center fixed max-lg:w-screen"}>
            {sending ?
                <div class="flex flex-row gap-2">
                    <div class="w-4 h-4 rounded-full bg-blue-700 animate-bounce [animation-delay:.7s]"></div>
                    <div class="w-4 h-4 rounded-full bg-blue-700 animate-bounce [animation-delay:.3s]"></div>
                    <div class="w-4 h-4 rounded-full bg-blue-700 animate-bounce [animation-delay:.7s]"></div>
                </div>:
                <>
                    {inEditingProcess === false ?
                        <>
                            <Input
                                value={messageText}
                                onChange={messageChange}
                                variant={"bordered"}
                                type={"text"}
                                size={"lg"}
                                className={"w-[30%] max-lg:w-[60%]"}
                                placeholder={"message..."}
                                disabled={!!temporaryURLPhoto || !!temporaryURLVideo}
                                endContent={
                                    <div className={"flex gap-3"}>
                                        <Autocomplete
                                            color={"default"}
                                            className="max-w-xs"
                                            defaultItems={languages}
                                            placeholder="Launguage"
                                            size={"sm"}
                                            onSelectionChange={setLanguage}>
                                            {(item) => <AutocompleteItem key={item.key}>{item.label}</AutocompleteItem>}
                                        </Autocomplete>
                                        <button disabled={!isCorrectMessageToCorrectIt}
                                                className={isCorrectMessageToCorrectIt ? "text-white" : "text-gray-500"}
                                                onClick={correctMessage}>
                                            <FontAwesomeIcon icon={faChevronUp}/>
                                        </button>
                                    </div>
                                }
                            />
                            {temporaryURLPhoto &&
                                <div
                                    className={"absolute bottom-[60px] left-1/2 -translate-x-1/2 max-w-[30%] max-h-[200px]"}>
                                    <img
                                        src={temporaryURLPhoto} alt={"photo"}
                                        className={"w-full h-auto max-h-[200px] object-contain rounded-lg shadow-lg"}/>
                                    <FontAwesomeIcon icon={faXmark}
                                                     className={"absolute top-1 right-1 bg-black/60 text-white p-1 rounded-full" +
                                                         " hover:bg-black/80 transition cursor-pointer"}
                                                     onClick={closeAllForPhoto}/>
                                </div>
                            }
                            {temporaryURLVideo &&
                                <div
                                    className={"absolute bottom-[60px] left-1/2 -translate-x-1/2 max-w-[30%] max-h-[200px]"}>
                                    <video src={temporaryURLVideo} controls
                                           className={"w-full h-auto max-h-[200px] object-contain rounded-lg shadow-lg"}/>
                                    <FontAwesomeIcon icon={faXmark}
                                                     className={"absolute top-1 right-1 bg-black/60 text-white p-1 rounded-full" +
                                                         " hover:bg-black/80 transition cursor-pointer"}
                                                     onClick={closeAllForVideo}/>
                                </div>}
                            <div
                                className={"relative left-3 w-[50px] h-[50px] bg-transparent flex items-center justify-center"}>
                                {messageText ?
                                    <button onClick={sendMessageText} disabled={!isMessageTextCorrect}>
                                        <FontAwesomeIcon icon={faPaperPlane} size={"2x"}
                                                         color={isMessageTextCorrect ? "white" : "gray"}/>
                                    </button> :
                                    <div>
                                        <Dropdown className={"bg-black/50"}>
                                            <DropdownTrigger>
                                                <Button className={"relative -right-5"} variant="light"><FontAwesomeIcon
                                                    icon={faImages}
                                                    size={"2x"}/></Button>
                                            </DropdownTrigger>
                                            <DropdownMenu aria-label="Action event example">
                                                <DropdownItem key="photo" className={"text-white"}
                                                              onClick={clickOnPhoto}>Photo</DropdownItem>
                                                <DropdownItem key="video" className={"text-white"}
                                                              onClick={clickOnVideo}>Video</DropdownItem>
                                            </DropdownMenu>
                                        </Dropdown>

                                        {photo && <Button className={"absolute left-[150%]"} variant={"light"}
                                                          onClick={sendingPhoto}>
                                            <FontAwesomeIcon
                                                icon={faPaperPlane}
                                                size={"2x"}/>
                                        </Button>}

                                        {video && <Button className={"absolute left-[150%]"} variant={"light"}
                                                          onClick={sendingVideo}>
                                            <FontAwesomeIcon
                                                icon={faPaperPlane}
                                                size={"2x"}/>
                                        </Button>}

                                        <input
                                            type={"file"}
                                            accept={"image/*"}
                                            className={"hidden"}
                                            ref={photoRef}
                                            onChange={photoChange}/>

                                        <input
                                            type={"file"}
                                            accept={"video/mp4"}
                                            className={"hidden"}
                                            ref={videoRef}
                                            onChange={videoChange}
                                        />
                                    </div>
                                }
                            </div>
                        </> :
                        <>
                            <Input
                                value={editedMessage}
                                onChange={messageEditChange}
                                variant={"bordered"}
                                type={"text"}
                                size={"lg"}
                                className={"w-[30%]"}
                                placeholder={"message..."}
                                endContent={<FontAwesomeIcon icon={faXmark} className={"cursor-pointer"}
                                                             onClick={() => escapeEdit()}/>}
                            />
                            <div
                                className={"relative left-3 w-[50px] h-[50px] bg-transparent flex items-center justify-center"}>
                                {editedMessage &&
                                    <button onClick={sendEditedMessage} disabled={!isEditedMessageCorrect}>
                                        <FontAwesomeIcon icon={faPen} size={"2x"}
                                                         color={isEditedMessageCorrect ? "white" : "gray"}/>
                                    </button>
                                }
                            </div>
                        </>
                    }
                </>
            }
        </div>
    )
}

const languages = [
    {label: "Ukrainian", key: "Ukrainian"},
    {label: "English", key: "English"},
    {label: "Russian", key: "Russian"},
    {label: "China", key: "China"},
    {label: "Armenian", key: "Armenian"},
    {label: "French", key: "French"}
]
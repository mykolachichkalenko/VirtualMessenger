import {Input, User} from "@heroui/react";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faSearch} from "@fortawesome/free-solid-svg-icons/faSearch";
import {useEffect, useState} from "react";
import {EventSourcePolyfill} from 'event-source-polyfill';
import {AnimatePresence, motion} from "framer-motion";

export default function Search() {
    const [requestForFinding, setRequestForFinding] = useState("");
    const [found, setFound] = useState([]);

    //searching the users or channels
    useEffect(() => {
        if (!requestForFinding) {
            setFound([]);
            return;
        }

        setFound([])
        const sse = new EventSourcePolyfill(`http://localhost:8080/get/users/${requestForFinding}`, {
            withCredentials: true
        });

        sse.onmessage = (ev) => {
            if (ev.data) {
                try {
                    const user = JSON.parse(ev.data);
                    setFound(prevState => {
                        if (prevState.some(prevUser => prevUser.id === user.id)) {
                            return prevState;
                        } else {
                            return [...prevState, user];
                        }
                    });
                } catch (error) {
                    console.error("Failed to parse SSE data:", error);
                }
            }
        };

        return () => {
            sse.close();
        };
    }, [requestForFinding]);
    return (
        <div className={"w-full flex justify-end relative right-10 top-1.5"}>
            <Input
                value={requestForFinding}
                size={"lg"}
                className={"w-[90%]"}
                endContent={<FontAwesomeIcon icon={faSearch}/>}
                placeholder="Enter user phone number"
                type={"text"}
                variant="bordered"
                onChange={(event) => setRequestForFinding(event.target.value)}
            />
            {requestForFinding &&
                <div className={"w-auto bg-[#0d0d0d] h-auto absolute top-[105%] border-1 rounded-2xl left-[10%]"}>
                    <div className={"flex p-2 flex-col items-start"}>
                        {found.length > 0 ?
                            <AnimatePresence>
                                {found.map(user => (
                                    <motion.div key={user.id}
                                                initial={{opacity: 0}}
                                                animate={{opacity: 1}}
                                                transition={{duration: 0.3}}
                                                exit={{y: -100, opacity: 0}}>
                                        <User
                                            className={"cursor-pointer hover:bg-black"}
                                            key={user.id}
                                            name={user.name}
                                            description={user.phoneNumber}
                                            avatarProps={{
                                                src: user.avatarUrl
                                            }}
                                        onClick={() => window.location.href=`/${user.phoneNumber}`}/>
                                    </motion.div>
                                ))}
                            </AnimatePresence>
                            :
                            <p className={"text-white/40 text-1xl"}>Nothing found</p>}
                    </div>
                </div>}
        </div>
    )
}
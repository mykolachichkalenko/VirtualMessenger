import {useEffect, useRef, useState} from "react";
import axios from "axios";
import {faPen, faRightFromBracket, faXmark} from "@fortawesome/free-solid-svg-icons";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";

export default function Profile() {
    const [user, setUser] = useState(null);
    const [editingName, setEditingName] = useState(false);
    const [newName, setNewName] = useState("");
    const fileInputRef = useRef(null);
    const [newPhoto, setNewPhoto] = useState(null);
    const [newPhotoPreview, setNewPhotoPreview] = useState("");

    useEffect(() => {
        axios.get("http://localhost:8080/get/get/me", {withCredentials: true})
            .then(resp => {
                setUser(resp.data);
                setNewName(resp.data.name);
            });
    }, []);

    const handlePhotoClick = () => {
        fileInputRef.current.click();
    };

    const handlePhotoChange = (event) => {
        if (event.target.files[0]) {
            setNewPhoto(event.target.files[0]);
            setNewPhotoPreview(URL.createObjectURL(event.target.files[0]));
        } else {
            setNewPhotoPreview("");
            setNewPhoto(null)
        }
    };

    const handleNameSave = () => {
        setEditingName(false);
    };

    const cancleEditPhoto = () => {
        setNewPhoto(null);
        setNewPhotoPreview("");
    }
    const cancleEdit = () => {
        setEditingName(false);
        setNewName(user.name)
    }

    const changeUser = () => {
        if (newPhoto && newName) {
            const formData = new FormData();
            formData.append("name", newName);
            formData.append("photo", newPhoto);

            axios.post("http://localhost:8080/get/change/user", formData,
                {withCredentials: true,
                    headers: {
                        "Content-Type": "multipart/form-data",
                    }})
                .then(resp => window.location.href = "/");
        }
    }
    return (
        <div
            className="w-screen h-screen flex justify-center items-center bg-gradient-to-br from-gray-900 via-indigo-900 to-black">
            <div
                className="bg-[#1a1a1a] w-[90%] md:w-[400px] min-h-[33%] rounded-2xl flex flex-col items-center p-6 shadow-2xl text-white relative"
            >
                <FontAwesomeIcon icon={faRightFromBracket} className={"self-end absolute top-5 cursor-pointer"}
                                 onClick={() => window.location.href = "/"}/>

                {user ? (
                    <>
                        <p className="text-sm text-gray-400 mt-4">{user.phoneNumber}</p>
                        <div className="relative group mt-8">
                            <img
                                src={newPhotoPreview || user.avatarUrl}
                                alt="avatar"
                                className="w-32 h-32 rounded-full object-cover border-4 border-white shadow-lg"
                            />
                            {newPhotoPreview && <FontAwesomeIcon onClick={cancleEditPhoto} icon={faXmark}
                                                                 className={"cursor-pointer absolute right-0 top-0"}/>}
                            <button
                                onClick={handlePhotoClick}
                                className="absolute bottom-2 left-1/2 -translate-x-1/2 bg-black bg-opacity-60 text-white px-3 py-1 text-xs rounded opacity-0 group-hover:opacity-100 transition-opacity"
                            >
                                <FontAwesomeIcon icon={faPen}/>
                            </button>
                            <input
                                type="file"
                                ref={fileInputRef}
                                style={{display: 'none'}}
                                onChange={handlePhotoChange}
                                accept="image/*"
                            />
                        </div>

                        <div className="mt-6 relative group w-full flex flex-col items-center">
                            {editingName ? (
                                <div className="flex gap-2 items-center">
                                    <input
                                        type="text"
                                        value={newName}
                                        onChange={(e) => setNewName(e.target.value)}
                                        className="text-black px-2 py-1 rounded"
                                    />
                                    <button onClick={cancleEdit}>
                                        <FontAwesomeIcon icon={faXmark}/>
                                    </button>
                                    <button
                                        onClick={handleNameSave}
                                        className="text-sm bg-green-700 px-2 py-1 rounded hover:bg-green-600"
                                    >
                                        Save
                                    </button>
                                </div>
                            ) : (
                                <>
                                    <h1 className="text-2xl font-bold">{newName}</h1>
                                    <button
                                        onClick={() => setEditingName(true)}
                                        className="text-xs mt-1 opacity-0 group-hover:opacity-100 transition-opacity text-blue-400 hover:text-blue-500"
                                    >
                                        <FontAwesomeIcon icon={faPen}/>
                                    </button>
                                </>
                            )}
                        </div>


                        <button className="mt-8 px-4 py-2 bg-blue-600 hover:bg-blue-700 rounded-xl"
                                onClick={changeUser}>
                            Save
                        </button>
                    </>
                ) : (
                    <p className="text-gray-400 mt-10">...</p>
                )}
            </div>
        </div>

    );
}
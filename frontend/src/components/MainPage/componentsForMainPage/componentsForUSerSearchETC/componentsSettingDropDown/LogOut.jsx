import ConfirmForm from "../../../../designedComponents/ConfirmForm.jsx";
import axios from "axios";

export default function LogOut({openIn,onClose,title,text}) {

    const onConfirm=()=>{
        axios.post("http://localhost:8080/auth/logout",{},{withCredentials:true})
            .then(response => window.location.reload());
    }
    return (
        <>
            {openIn && <ConfirmForm onClose={onClose} title={title} text={text} onConfirm={() => onConfirm()}/>}
        </>
    )
}


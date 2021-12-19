import React, { useState } from 'react'
import PropTypes from 'prop-types'
import axios from 'axios';

const VoteForm = ({ setOutput = () => null }) => {
    const [registerId, setRegisterId] = useState("");
    const [firstName, setFirstName] = useState("");
    const [lastName, setLastName] = useState("");

    const handleSubmit = () => {
        const body = {
            registerId,
            firstName,
            lastName,
        }
        console.log(body);
        axios.post(`/api/submit/createVoter`, body)
            .then(res => setOutput(res.data.output))
            .catch(error => console.log(error));
    };

    return (
        <div className="space-y-5">
            <div className="gap-1 flex flex-col">
                <span className="font-medium text-sm">Número único de registro</span>
                <input type="text" onChange={e => setRegisterId(e.currentTarget.value)} />
            </div>
            <div className="gap-1 flex flex-col">
                <span className="font-medium text-sm">Nombre</span>
                <input type="text" onChange={e => setFirstName(e.currentTarget.value)} />
            </div>
            <div className="gap-1 flex flex-col">
                <span className="font-medium text-sm">Apellido</span>
                <input type="text" onChange={e => setLastName(e.currentTarget.value)} />
            </div>
            <button onClick={handleSubmit} type="button" className="bg-zinc-600 hover:opacity-40 px-5 py-2 text-zinc-200">Enviar</button>
        </div>
    )
}

VoteForm.propTypes = {
    setOutput: PropTypes.func,
}

export default VoteForm


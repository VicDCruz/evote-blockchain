import React, { useState } from 'react'
import PropTypes from 'prop-types'
import axios from 'axios';

const VoteForm = ({ voterId = "", setOutput = () => null }) => {
    const [picked, setPicked] = useState("");
    const [electionId, setElectionId] = useState("");

    const handleSubmit = () => {
        const body = {
            picked,
            electionId,
            voterId,
        }
        console.log(body);
        axios.post(`/api/submit/castVote`, body)
            .then(res => setOutput(res.data.output))
            .catch(error => console.log(error));
    };

    return (
        <div className="space-y-5">
            <div className="gap-1 flex flex-col">
                <span className="font-medium text-sm">Voto por</span>
                <input type="text" onChange={e => setPicked(e.currentTarget.value)} />
            </div>
            <div className="gap-1 flex flex-col">
                <span className="font-medium text-sm">Elección</span>
                <input type="text" onChange={e => setElectionId(e.currentTarget.value)} />
            </div>
            <button onClick={handleSubmit} type="button" className="bg-zinc-600 hover:opacity-40 px-5 py-2 text-zinc-200">Enviar</button>
        </div>
    )
}

VoteForm.propTypes = {
    voterId: PropTypes.string,
    setOutput: PropTypes.func,
}

export default VoteForm


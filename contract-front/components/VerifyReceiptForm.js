import React, { useState } from 'react'
import PropTypes from 'prop-types'
import axios from 'axios';

const VoteForm = ({ setOutput = () => null }) => {
    const [receiptId, setReceiptId] = useState("");

    const handleSubmit = () => {
        const body = { receiptId };
        console.log(body);
        axios.post(`/api/submit/verifyReceipt`, body)
            .then(res => setOutput(res.data.output))
            .catch(error => console.log(error));
    };

    return (
        <div className="space-y-5">
            <div className="gap-1 flex flex-col">
                <span className="font-medium text-sm">Recibo ID</span>
                <input type="text" onChange={e => setReceiptId(e.currentTarget.value)} />
            </div>
            <button onClick={handleSubmit} type="button" className="bg-zinc-600 hover:opacity-40 px-5 py-2 text-zinc-200">Enviar</button>
        </div>
    )
}

VoteForm.propTypes = {
    setOutput: PropTypes.func,
}

export default VoteForm


import React, { Component } from 'react';
import { Result, Button, Table } from 'antd';
import Axios from 'axios';
import DeleteRadioStationButton from './deleteStation/DeleteRadioStationButton';
import {
    withRouter
} from 'react-router-dom'
import { RADIO_STATIONS } from '../../layouts/pathTypes';

const columns = [
    {
        title: 'Id',
        dataIndex: 'radioStation.id',
        width: '10%',
    },
    {
        title: 'Title',
        dataIndex: 'radioStation.title',
        width: '40%',
    },
    {
        title: 'Actions',
        key: 'operation',
        fixed: 'right',
        render: (text, record) => {
            const id = record.radioStation.id;
            return (<DeleteRadioStationButton key={id} id={id} />)
        },
    }
];

class RadioStationsTable extends Component {

    state = {
        data: [],
        pagination: {
            total: null,
            pageSize: null,
            current: null
        },
        filter: {
            page: 0,
            size: 10,
        },
        error: false,
        loading: true,
    };

    componentDidMount() {
        this.loadDataWithSearchParams();
        this.props.history.listen(() => {
            this.loadDataWithSearchParams();
        });
    }

    loadDataWithSearchParams = () => {
        const params = new URLSearchParams(window.location.search);

        let page = params.get('page');
        if (!page) {
            page = 1;
        }

        let size = params.get('size');
        if (!size) {
            size = 10;
        }

        const filter = {
            page: page - 1,
            size,
        };

        this.setState(
            { ...this.state, filter },
            this.loadData
        );
    }

    loadData = () => {
        this.setState({ ...this.state, loading: true, error: false });

        const urlSearchParams = new URLSearchParams();
        urlSearchParams.set('sort', 'id,desc');
        urlSearchParams.set('page', this.state.filter.page);
        urlSearchParams.set('size', this.state.filter.size);

        Axios.get('/radio-stations?' + urlSearchParams.toString())
            .then((response) => {
                let data = [];

                if (response.data._embedded && response.data._embedded.radioStationResourceList) {
                    data = response.data._embedded.radioStationResourceList;
                }

                this.setState({
                    ...this.state,
                    data,
                    pagination: {
                        total: response.data.page.totalElements,
                        pageSize: response.data.page.size,
                        current: response.data.page.number + 1
                    }
                })

            })
            .catch(() => { this.setState({ ...this.state, error: true }) })
            .then(() => this.setState({ ...this.state, loading: false }));
    }

    handleTableChange = (page) => {
        const urlSearchParams = new URLSearchParams();
        urlSearchParams.set('page', page.current);
        urlSearchParams.set('size', page.pageSize);

        this.props.history.push(RADIO_STATIONS + '?' + urlSearchParams.toString());
    }

    render() {
        if (this.state.error) {
            return (
                <Result
                    status="error"
                    title="Failed to load radio stations"
                    subTitle="Please wait until service will be working again"
                    extra={[
                        <Button type="primary" key="console" onClick={this.loadData}>Retry</Button>,
                    ]}
                >
                </Result>
            );
        }
        return (
            <Table
                columns={columns}
                rowKey={record => record.radioStation.id}
                dataSource={this.state.data}
                pagination={this.state.pagination}
                loading={this.state.loading}
                onChange={this.handleTableChange}
            />
        );
    }
}

export default withRouter(RadioStationsTable);
import React from "react";
import $ from "jquery";
import {DeleteStatusMessageMixin} from "jsx/mixin";
import Modal from 'react-bootstrap/lib/Modal';
import Button from 'react-bootstrap/lib/Button';

const NodeStatus = React.createClass({
  propTypes: {
    endpointStatus: React.PropTypes.object.isRequired,
    clusterName: React.PropTypes.string.isRequired
  },

  getInitialState() {
    return { showModal: false };
  },

  close() {
    this.setState({ showModal: false });
  },

  open() {
    this.setState({ showModal: true });
  },

  render: function() {
    
    let buttonStyle = "btn btn-xs btn-success";
    let largeButtonStyle = "btn btn-lg btn-success";

    if(this.props.endpointStatus.status!='NORMAL'){
      buttonStyle = "btn btn-xs btn-danger";
      largeButtonStyle = "btn btn-lg btn-danger";
    }

    function humanFileSize(bytes, si) {
    var thresh = si ? 1000 : 1024;
    if(Math.abs(bytes) < thresh) {
        return bytes + ' B';
    }
    var units = si
        ? ['kB','MB','GB','TB','PB','EB','ZB','YB']
        : ['KiB','MiB','GiB','TiB','PiB','EiB','ZiB','YiB'];
    var u = -1;
    do {
        bytes /= thresh;
        ++u;
    } while(Math.abs(bytes) >= thresh && u < units.length - 1);
    return bytes.toFixed(1)+' '+units[u];
}

    return (<span>
            <button type="button" className={buttonStyle} onClick={this.open}>{this.props.endpointStatus.endpoint}</button>
            <Modal show={this.state.showModal} onHide={this.close}>
              <Modal.Header closeButton>
                <Modal.Title>Endpoint {this.props.endpointStatus.endpoint}</Modal.Title>
              </Modal.Header>
              <Modal.Body>
                <h4>Host id</h4>
                <p>{this.props.endpointStatus.hostId}</p>
                <h4>Datacenter / Rack</h4>
                <p>{this.props.endpointStatus.dc} / {this.props.endpointStatus.rack}</p>
                <h4>Release version</h4>
                <p>{this.props.endpointStatus.releaseVersion}</p>
                <h4>Tokens</h4>
                <p>{this.props.endpointStatus.tokens}</p>
                <h4>Status</h4>
                <p><button type="button" className={largeButtonStyle}>{this.props.endpointStatus.status}</button></p>
                <h4>Severity</h4>
                <p>{this.props.endpointStatus.severity}</p>
                <h4>load</h4>
                <p>{humanFileSize(this.props.endpointStatus.load, 1024)}</p>
              </Modal.Body>
              <Modal.Footer>
                <Button onClick={this.close}>Close</Button>
              </Modal.Footer>
            </Modal>
          </span>
    );

    }

})


const TableRow = React.createClass({

  propTypes: {
    name: React.PropTypes.string.isRequired
  },
  
  getInitialState: function() {
    const isDev = window != window.top;
    const URL_PREFIX = isDev ? 'http://127.0.0.1:8080' : '';
    return {clusterStatus: {}, clusterStatuses: null, urlPrefix: URL_PREFIX};
  },

  componentWillMount: function() {
    this._refreshClusterStatus();
    this.setState({clusterStatuses: setInterval(this._refreshClusterStatus, 10000)}); 
  },

  _refreshClusterStatus: function() {
    $.ajax({
          url: this.state.urlPrefix + '/cluster/' + encodeURIComponent(this.props.name),
          method: 'GET',
          component: this,
          complete: function(data) {
            this.component.setState({clusterStatus: $.parseJSON(data.responseText)});
          }
      });
  },

  componentWillUnmount: function() {
    clearInterval(this.clusterStatuses);
  },

  render: function() {

  let nodes="";
  if(this.state.clusterStatus.nodes_status) {
    nodes = this.state.clusterStatus.nodes_status.endpointStates[0].endpoints.map(endpoint =>
        <NodeStatus key={endpoint.endpoint} endpointStatus={endpoint} clusterName={this.props.name}/>
    );
  }

    let rowDivStyle = {
      marginLeft: "0"
    };

    return (
    <tr>
        <td width="20%"><a href={'repair.html?currentCluster=' + this.props.name}> {this.props.name}</a></td>
        <td width="80%"><div className="row" style={rowDivStyle}>{nodes}</div></td>
        <td width="20%">
          <button type="button" className="btn btn-xs btn-danger" onClick={this._onDelete}>Delete</button>
        </td>
    </tr>
    );
  },

  _onDelete: function(e) {
    this.props.deleteSubject.onNext(this.props.name);
  }

});

const clusterList = React.createClass({
  mixins: [DeleteStatusMessageMixin],

  propTypes: {
    clusterNames: React.PropTypes.object.isRequired,
    deleteSubject: React.PropTypes.object.isRequired,
    deleteResult: React.PropTypes.object.isRequired
  },

  getInitialState: function() {
    return {clusterNames: [], deleteResultMsg: null};
  },

  componentWillMount: function() {
    this._clusterNamesSubscription = this.props.clusterNames.subscribeOnNext(obs =>
      obs.subscribeOnNext(names => this.setState({clusterNames: names}))
    );
  },

  componentWillUnmount: function() {
    this._clusterNamesSubscription.dispose();
  },

  render: function() {

    const rows = this.state.clusterNames.map(name =>
      <TableRow name={name} key={name} deleteSubject={this.props.deleteSubject} getClusterStatus={this.props.getClusterStatus} getClusterSubject={this.props.getClusterSubject}/>);

    let table = null;
    if(rows.length == 0) {
      table = <div className="alert alert-info" role="alert">No clusters found</div>
    } else {

      table = <div className="row">
          <div className="col-lg-12">
              <div className="table-responsive">
                  <table className="table table-bordered table-hover table-striped">
                      <thead>
                          <tr>
                              <th>Cluster Name</th>
                              <th>Status</th>
                              <th></th>
                          </tr>
                      </thead>
                      <tbody>
                        {rows}
                      </tbody>
                  </table>
              </div>
          </div>
      </div>;
    }

    return (<div className="panel panel-default">
              <div className="panel-body">
                {this.deleteMessage()}
                {table}
              </div>
            </div>);
  }
});

export default clusterList;
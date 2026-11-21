import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App.jsx';
import * as monaco from 'monaco-editor';
import { loader } from '@monaco-editor/react';
import EditorWorker from 'monaco-editor/esm/vs/editor/editor.worker?worker';

// Bundle Monaco locally instead of loading its code from a CDN.
// This also prevents an empty editor when the CDN is blocked or offline.
self.MonacoEnvironment = { getWorker: () => new EditorWorker() };
loader.config({ monaco });
import './styles.css';

class AppErrorBoundary extends React.Component {
  constructor(props) { super(props); this.state = {error:null}; }
  static getDerivedStateFromError(error) { return {error}; }
  componentDidCatch(error, info) { console.error('TAVQEN rendering error:', error, info); }
  render() {
    if (this.state.error) return <div style={{background:'#071426',color:'#fff',minHeight:'100vh',padding:32,fontFamily:'system-ui'}}><h2>TAVQEN could not load</h2><p style={{color:'#fca5a5'}}>{this.state.error.message}</p><p>Open browser DevTools (F12) → Console to see details.</p><button onClick={() => location.reload()}>Reload</button></div>;
    return this.props.children;
  }
}
ReactDOM.createRoot(document.getElementById('root')).render(<AppErrorBoundary><App/></AppErrorBoundary>);

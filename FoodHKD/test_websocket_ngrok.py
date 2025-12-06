#!/usr/bin/env python3
import websocket
import json
import time
import sys

# Test WebSocket connection via ngrok
def test_websocket_ngrok():
    ngrok_url = "wss://unmalted-alphonso-unpreponderated.ngrok-free.dev/api/ws/orders"
    localhost_url = "ws://localhost:8080/api/ws/orders"
    
    print("=" * 60)
    print("WebSocket Connection Test")
    print("=" * 60)
    
    # Test localhost connection
    print("\n1. Testing localhost connection...")
    try:
        ws_local = websocket.WebSocket()
        ws_local.connect(localhost_url)
        print("✅ Localhost connection SUCCESS")
        
        # Receive initial message
        msg = ws_local.recv()
        print(f"   Received: {msg}")
        
        # Send subscription message
        sub_msg = json.dumps({"action": "subscribe"})
        ws_local.send(sub_msg)
        print(f"   Sent: {sub_msg}")
        
        # Receive response
        response = ws_local.recv()
        print(f"   Response: {response}")
        
        ws_local.close()
        print("   Connection closed")
    except Exception as e:
        print(f"❌ Localhost connection FAILED: {type(e).__name__}: {e}")
    
    # Test ngrok connection
    print("\n2. Testing ngrok (HTTPS) connection...")
    try:
        ws_ngrok = websocket.WebSocket()
        ws_ngrok.connect(ngrok_url)
        print("✅ Ngrok connection SUCCESS")
        
        # Receive initial message
        msg = ws_ngrok.recv()
        print(f"   Received: {msg}")
        
        # Send subscription message
        sub_msg = json.dumps({"action": "subscribe"})
        ws_ngrok.send(sub_msg)
        print(f"   Sent: {sub_msg}")
        
        # Receive response
        response = ws_ngrok.recv()
        print(f"   Response: {response}")
        
        ws_ngrok.close()
        print("   Connection closed")
    except Exception as e:
        print(f"❌ Ngrok connection FAILED: {type(e).__name__}: {e}")
    
    print("\n" + "=" * 60)
    print("Test Complete")
    print("=" * 60)

if __name__ == "__main__":
    test_websocket_ngrok()

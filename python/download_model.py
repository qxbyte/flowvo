#!/usr/bin/env python3
"""
下载嵌入模型到本地
"""

import os
from sentence_transformers import SentenceTransformer

def download_model():
    """下载模型到本地"""
    
    # 项目路径
    base_dir = os.path.dirname(os.path.abspath(__file__))
    project_root = os.path.dirname(base_dir)
    models_dir = os.path.join(project_root, "models")
    
    # 确保models目录存在
    os.makedirs(models_dir, exist_ok=True)
    
    print("=== FlowVO 模型下载工具 ===")
    print(f"下载目录: {models_dir}")
    
    # 下载模型
    model_name = "sentence-transformers/all-mpnet-base-v2"
    local_path = os.path.join(models_dir, "all-mpnet-base-v2")
    
    print(f"正在下载模型: {model_name}")
    print(f"保存路径: {local_path}")
    
    try:
        # 下载并保存模型
        model = SentenceTransformer(model_name)
        model.save(local_path)
        
        print("✅ 模型下载成功！")
        
        # 测试模型
        print("测试模型...")
        test_model = SentenceTransformer(local_path, local_files_only=True)
        embedding = test_model.encode(["测试文本"])
        print(f"✅ 模型测试成功，向量维度: {len(embedding[0])}")
        
    except Exception as e:
        print(f"❌ 模型下载失败: {e}")
        return False
    
    return True

if __name__ == "__main__":
    success = download_model()
    if not success:
        exit(1) 
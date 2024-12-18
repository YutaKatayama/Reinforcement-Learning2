# 3Dグラフを描画するプログラム
# データ多すぎて分かりずらかった
import pandas as pd
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D

def main():
    # ユーザーにCSVファイルのパスを入力させる
    print("=== Q学習データの3Dプロット ===")
    file_path = input("CSVファイルのパスを入力してください（例: output.csv）:\n")

    try:
        # CSVファイルを読み込む
        df = pd.read_csv(file_path, header=None, names=['データ'])
        
        # データを分割して新しいカラムを作成
        df[['ε', 'α', 'γ', '学習回数', 'ステップ数']] = df['データ'].str.extract(r'ε:(\d+\.\d+) α:(\d+\.\d+) γ:(\d+\.\d+) 学習回数:(\d+) ステップ数:(\d+)')
        
        # データ型を変換
        df['ε'] = df['ε'].astype(float)
        df['α'] = df['α'].astype(float)
        df['γ'] = df['γ'].astype(float)
        df['学習回数'] = df['学習回数'].astype(int)
        df['ステップ数'] = df['ステップ数'].astype(int)

        # 3Dプロットの作成
        fig = plt.figure()
        ax = fig.add_subplot(111, projection='3d')

        # プロットするデータ
        scatter = ax.scatter(df['ε'], df['α'], df['γ'], c=df['学習回数'], cmap='viridis', marker='o')

        # 軸ラベルの設定
        ax.set_xlabel('ε (イプシロン)')
        ax.set_ylabel('α (アルファ)')
        ax.set_zlabel('γ (ガンマ)')
        ax.set_title('Q学習のパラメータの3Dプロット')

        # カラーバーの追加
        cbar = plt.colorbar(scatter, ax=ax, pad=0.1)
        cbar.set_label('学習回数')

        # プロットの表示
        plt.show()

    except FileNotFoundError:
        print(f"ファイルが見つかりません: {file_path}")
    except Exception as e:
        print(f"エラーが発生しました: {e}")

if __name__ == "__main__":
    main()
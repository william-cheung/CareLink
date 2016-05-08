package com.carelink.ui;

import java.io.File;
import java.util.ArrayList;

import com.carelink.R;
import com.carelink.interaction.Services;
import com.carelink.model.VersionInfo;
import com.carelink.ui.UpdateAppTask.UpdateAppCallback;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

@SuppressLint("InflateParams") 
public class PercenterFragment extends Fragment {
	
	private UpdateAppTask updateAppTask = null; 
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_percenter, container, false);

		ArrayList<ListItem> listItems = new ArrayList<ListItem>();
		listItems.add(new ListItem(R.string.text_health_profile, new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(getActivity(), HealthProfileActivity.class));
			}
		}));

		listItems.add(new ListItem(R.string.text_account_info, new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(getActivity(), AccountInfoActivity.class));
			}
		}));

		listItems.add(new ListItem(R.string.text_connect_a_meter, new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(getActivity(), ConnectMetersActivity.class));
			}
		}));

		listItems.add(new ListItem(R.string.text_send_us_feedback, new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(getActivity(), SendFeedbackActivity.class));
			}
		}));

		listItems.add(new ListItem(R.string.text_check_updates, new OnClickListener() {
			public void onClick(View v) {
				// MyApplication.showMessageBox(getActivity(), R.string.message_newest_version);
				showUpdateDialog();
			}
		}));
		listItems.add(new ListItem(R.string.text_about_carelink, new OnClickListener() {
			public void onClick(View v) {
				showAboutDialog();
			}
		}));
		listItems.add(new ListItem(R.string.text_contact_us, new OnClickListener() {
			public void onClick(View v) {
				showContactDialog();
			}
		}));

		ListView listView = (ListView) view.findViewById(R.id.listView);
		listView.setAdapter(new ListAdapter(getActivity(), listItems));

		view.findViewById(R.id.button_logout).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Services.getInstance(getActivity()).signOut();
				startActivity(new Intent(getActivity(), SignInActivity.class));
				getActivity().finish();
			}
		});

		return view;
	}


	private class ListItem {
		private int textResID;
		private OnClickListener onClickListener;
		public ListItem(int textResID, OnClickListener onClickListener) {
			this.textResID = textResID;
			this.onClickListener = onClickListener;
		}

		public int getTextResID() {
			return textResID;
		}

		public OnClickListener getOnClickListener() {
			return onClickListener;
		}
	}

	private class ListAdapter extends BaseAdapter {
		private Context context;
		private ArrayList<ListItem> items;

		public ListAdapter(Context context, ArrayList<ListItem> items) {
			super();
			this.context = context;
			this.items = items;
		}

		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public Object getItem(int position) {
			return items.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater mInflater = (LayoutInflater)
						context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
				convertView = mInflater.inflate(R.layout.percenter_list_item, null);
			}
			TextView textView = (TextView) convertView.findViewById(R.id.textView);
			textView.setText(items.get(position).getTextResID());
			convertView.setOnClickListener(items.get(position).getOnClickListener());
			return convertView;
		}
	}

	private void showAboutDialog() {
		final AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
		dialog.show();
		Window window = dialog.getWindow();
		window.setContentView(R.layout.dialog_about_carelink);
		TextView versionNameTextView = (TextView) window.findViewById(R.id.textView_versionName);
		String versionName = MyApplication.getVersionName();
		if (!versionName.equals("")) {
			versionNameTextView.setText("Version " + versionName);
		} else {
			versionNameTextView.setText("");
		}
		window.findViewById(R.id.button_ok).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
	}

	private void showContactDialog() {
		final AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
		dialog.show();
		Window window = dialog.getWindow();
		window.setContentView(R.layout.dialog_contact_us);
		window.findViewById(R.id.button_ok).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
	}
	
	private void showUpdateDialog() {
		final AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
		dialog.setCancelable(false);
		dialog.show();
		Window window = dialog.getWindow();
		window.setContentView(R.layout.dialog_update);
		final ProgressBar progressBar = (ProgressBar) window.findViewById(R.id.progressBar);
		final TextView statusTextView = (TextView) window.findViewById(R.id.textView_status);
		final View buttonsView = window.findViewById(R.id.view_buttons);
		buttonsView.setVisibility(View.GONE);
		final Button positiveButton = (Button) window.findViewById(R.id.button_positive);
		final View negativeButtonView = window.findViewById(R.id.view_negativeButton);
		final Button negativeButton = (Button) window.findViewById(R.id.button_negative);
		negativeButtonView.setVisibility(View.GONE);
		statusTextView.setText(R.string.status_checking_for_update);
		
		updateAppTask = new UpdateAppTask(getActivity(), new UpdateAppCallback() {
			
			@Override
			public void onUpdateFound(VersionInfo versionInfo) {
				progressBar.setVisibility(View.GONE);
				buttonsView.setVisibility(View.VISIBLE);
				negativeButtonView.setVisibility(View.VISIBLE);
				
				String status = String.format(getString(R.string.status_question_update_format), 
						versionInfo.getVersionName());
				statusTextView.setText(status);
				
				positiveButton.setText(R.string.text_yes);
				positiveButton.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						if (updateAppTask != null) {
							updateAppTask.startDownloading();
						}
					}
				});
				negativeButton.setText(R.string.text_no);
				negativeButton.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
			}
			
			@Override
			public void onNoUpdateFound() {
				progressBar.setVisibility(View.GONE);
				buttonsView.setVisibility(View.VISIBLE);
				
				statusTextView.setText(R.string.status_newest_version);
				
				positiveButton.setText(R.string.text_ok);
				positiveButton.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
			}
			
			@Override
			public void onDowloading(int percent) {
				progressBar.setVisibility(View.VISIBLE);
				buttonsView.setVisibility(View.GONE);
				
				String status = String.format(getString(R.string.status_dowloading_new_version_format), percent);
				statusTextView.setText(status);
			}
			
			@Override
			public void onCompleteDownloading(File apkFile) {
				progressBar.setVisibility(View.GONE);
				buttonsView.setVisibility(View.VISIBLE);
				negativeButtonView.setVisibility(View.GONE);
				
				statusTextView.setText(R.string.status_complete_downloading);
				
				positiveButton.setText(R.string.text_ok);
				final File file = apkFile;
				positiveButton.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						dialog.dismiss();
						updateAppTask.installApk(file);
					}
				});
			}
			
			@Override
			public void onCheckUpdate() {
				progressBar.setVisibility(View.VISIBLE);
				buttonsView.setVisibility(View.GONE);
				
				statusTextView.setText(R.string.status_checking_for_update);
			}

			@Override
			public void onCheckUpdateError() {
				progressBar.setVisibility(View.GONE);
				buttonsView.setVisibility(View.VISIBLE);
				negativeButtonView.setVisibility(View.GONE);
				
				statusTextView.setText(R.string.status_network_error);
				
				positiveButton.setText(R.string.text_ok);
				positiveButton.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
			}

			@Override
			public void onDownloadApkError() {
				progressBar.setVisibility(View.GONE);
				buttonsView.setVisibility(View.VISIBLE);
				negativeButtonView.setVisibility(View.GONE);
				
				statusTextView.setText(R.string.status_downloading_failed);
				
				positiveButton.setText(R.string.text_ok);
				positiveButton.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
			}
		});
		updateAppTask.execute();
	}
}
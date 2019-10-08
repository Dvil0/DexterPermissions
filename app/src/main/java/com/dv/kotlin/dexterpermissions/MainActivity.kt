package com.dv.kotlin.dexterpermissions

import android.Manifest
import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.dv.kotlin.dexterpermissions.enums.PermissionStatusEnum
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.CompositeMultiplePermissionsListener
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.CompositePermissionListener
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener
import com.karumi.dexter.listener.single.PermissionListener
import com.karumi.dexter.listener.single.SnackbarOnDeniedPermissionListener
import kotlinx.android.synthetic.main.permission_layout.*

class MainActivity: Activity(){

    override fun onCreate( savedInstanceState: Bundle? ) {
        super.onCreate( savedInstanceState )
        setContentView( R.layout.permission_layout )

        setButtonClicks()

    }

    private fun setButtonClicks(){
//        btnCamera.setOnClickListener{ setCameraPermissionHandlerWithDialog() }
        btnCamera.setOnClickListener{ setCameraPermissionHandlerWithSnackbar() }
//        btnCamera.setOnClickListener{ checkCameraPermission() }
        btnContacts.setOnClickListener { checkContactsPermission() }
        btnAudio.setOnClickListener { checkAudioPermission() }
        btnAll.setOnClickListener { checkAllPermissions() }
    }

    private fun setPermissionHandler( permission: String, txtView: TextView){
        val context = this
        Dexter.withActivity( this )
            .withPermission( permission )
            .withListener( object: PermissionListener{
                override fun onPermissionGranted( response: PermissionGrantedResponse ){
                    setPermissionStatus( txtView, PermissionStatusEnum.GRANTED )
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {

                    if( response!!.isPermanentlyDenied ){
                        setPermissionStatus( txtView, PermissionStatusEnum.PERMANENTLY_DENIED )
                    } else {
                        setPermissionStatus( txtView, PermissionStatusEnum.DENIED )
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {
                    token!!.continuePermissionRequest()
                }
            } ).check()
    }

    private fun setPermissionStatus( txtView: TextView, status: PermissionStatusEnum){
        when( status ){
            PermissionStatusEnum.GRANTED -> {
                txtView.text = getString( R.string.perm_sts_granted )
                txtView.setTextColor( ContextCompat.getColor( this, R.color.colorPermissionGranted ) )
            }
            PermissionStatusEnum.DENIED ->{
                txtView.text = getString( R.string.perm_sts_denied )
                txtView.setTextColor(ContextCompat.getColor(this, R.color.colorPermissionDenied))
            }
            PermissionStatusEnum.PERMANENTLY_DENIED ->{
                txtView.text = getString( R.string.perm_sts_perm_denied )
                txtView.setTextColor( ContextCompat.getColor( this, R.color.colorPermissionPermanentlyDenied ))
            }
        }
    }

    private fun checkCameraPermission() = setPermissionHandler( Manifest.permission.CAMERA, txtCamera )

    private fun checkContactsPermission() = setPermissionHandler( Manifest.permission.READ_CONTACTS, txtContacts )

    private fun checkAudioPermission() = setPermissionHandler( Manifest.permission.RECORD_AUDIO, txtAudio )

    private fun checkAllPermissions(){
        Dexter.withActivity( this )
            .withPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.RECORD_AUDIO)
            .withListener( object: MultiplePermissionsListener{
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    report?.let {
                        for( permission in report.grantedPermissionResponses ){
                            when( permission.permissionName ){
                                Manifest.permission.CAMERA -> setPermissionStatus( txtCamera, PermissionStatusEnum.GRANTED )
                                Manifest.permission.READ_CONTACTS -> setPermissionStatus( txtCamera, PermissionStatusEnum.DENIED )
                                Manifest.permission.RECORD_AUDIO -> setPermissionStatus( txtCamera, PermissionStatusEnum.PERMANENTLY_DENIED )
                            }
                        }
                        for( permission in report.deniedPermissionResponses ){
                            when( permission.permissionName ){
                                Manifest.permission.CAMERA -> {

                                    if( permission.isPermanentlyDenied ){
                                        setPermissionStatus(txtCamera, PermissionStatusEnum.PERMANENTLY_DENIED)
                                    } else {
                                        setPermissionStatus(txtCamera, PermissionStatusEnum.DENIED)
                                    }
                                }
                                Manifest.permission.READ_CONTACTS -> {

                                    if( permission.isPermanentlyDenied ){
                                        setPermissionStatus(txtContacts, PermissionStatusEnum.PERMANENTLY_DENIED)
                                    } else {
                                        setPermissionStatus(txtContacts, PermissionStatusEnum.DENIED)
                                    }
                                }
                                Manifest.permission.RECORD_AUDIO -> {

                                    if( permission.isPermanentlyDenied ){
                                        setPermissionStatus(txtAudio, PermissionStatusEnum.PERMANENTLY_DENIED)
                                    } else {
                                        setPermissionStatus(txtAudio, PermissionStatusEnum.DENIED)
                                    }
                                }
                            }
                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            }).check()
    }

    private fun setCameraPermissionHandlerWithDialog(){
        val dialogPermissionListener = DialogOnDeniedPermissionListener.Builder
            .withContext( this )
            .withTitle("Camera Permission" )
            .withMessage( "Camera permission is needed to take pictures" )
            .withButtonText( android.R.string.ok )
            .withIcon( R.mipmap.ic_launcher )
            .build()

        val permission = object: PermissionListener{
            override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                setPermissionStatus( txtCamera, PermissionStatusEnum.GRANTED)
            }

            override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                if( response!!.isPermanentlyDenied ){
                    setPermissionStatus( txtCamera, PermissionStatusEnum.PERMANENTLY_DENIED)
                } else {
                    setPermissionStatus( txtCamera, PermissionStatusEnum.DENIED)
                }
            }

            override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) {
                token?.continuePermissionRequest()
            }
        }

        val composite = CompositePermissionListener( permission, dialogPermissionListener )

        Dexter.withActivity(this)
            .withPermission( Manifest.permission.CAMERA )
            .withListener( composite )
            .check()
    }

    private fun setCameraPermissionHandlerWithSnackbar(){
        val snackbarPermissionListener = SnackbarOnDeniedPermissionListener.Builder
            .with( root, "Camera is needed to take pictures" )
            .withOpenSettingsButton( "Settings" )
            .withCallback( object: Snackbar.Callback(){
                override fun onShown(sb: Snackbar?) {
                    // Event handler
                }

                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    // Event handler
                }
            }).build()

        val permission = object: PermissionListener{
            override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                setPermissionStatus( txtCamera, PermissionStatusEnum.GRANTED)
            }

            override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                if( response!!.isPermanentlyDenied ){
                    setPermissionStatus( txtCamera, PermissionStatusEnum.PERMANENTLY_DENIED)
                } else {
                    setPermissionStatus( txtCamera, PermissionStatusEnum.DENIED)
                }
            }

            override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) {
                token?.continuePermissionRequest()
            }
        }

        val composite = CompositePermissionListener( permission, snackbarPermissionListener )

        Dexter.withActivity(this)
            .withPermission( Manifest.permission.CAMERA )
            .withListener( composite )
            .check()
    }
}